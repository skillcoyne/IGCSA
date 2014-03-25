package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.mapreduce.FragmentPartitioner;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.mapreduce.SegmentOrderComparator;
import org.lcsb.lu.igcsa.mapreduce.fasta.ChromosomeSequenceMapper;
import org.lcsb.lu.igcsa.mapreduce.fasta.ChromosomeSequenceReducer;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;

import java.io.IOException;
import java.util.ArrayList;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class NormalChromosomeJob extends JobIGCSA
  {
  static Logger log = Logger.getLogger(NormalChromosomeJob.class.getName());

  private Path output;
  private String chr, genomeName, parent;

  public NormalChromosomeJob(Configuration conf)
    {
    super(conf);
    }

  public void NormalChromosomeJob()
    {
    Option genome = new Option("g", "genome", true, "Genome name.");
    genome.setRequired(true);
    this.addOptions(genome);

    Option chr = new Option("c", "chromosome", true, "chromosome name.");
    chr.setRequired(true);
    this.addOptions(chr);

    Option output = new Option("o", "Output path", true, "Fully qualified path in HDFS or S3 to write FASTA files.");
    output.setRequired(true);
    this.addOptions(output);
    }

  private Scan setup(String[] args) throws IOException, ParseException
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());

    genomeName = cl.getOptionValue("g");
    output = new Path(new Path(cl.getOptionValue("o"), Paths.GENOMES.getPath()), genomeName);

    chr = cl.getOptionValue("c");

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    GenomeResult genome = admin.getGenomeTable().getGenome(genomeName);
    if (genome == null) throw new IOException("No genome found for " + genomeName + ". Exiting.");
    else parent = genome.getParent();

    Scan scan = admin.getSequenceTable().getScanFor(new Column("info", "genome", genomeName), new Column("chr", "name", chr));
    scan.setCaching(100);

    return scan;
    }

  public Job createJob(String[] args) throws IOException, ParseException
    {
    Scan scan = setup(args);
    /* Set up job */
    Job job = new Job(getConf(), "Generate FASTA file for chromosome " + chr + " in " + genomeName);
    job.setJarByClass(NormalChromosomeJob.class);

    job.setMapperClass(ChromosomeSequenceMapper.class);
    ChromosomeSequenceMapper.setChromosomes(job, new String[]{chr});

    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf()).getSequenceTable().getTableName(), scan,
                                          ChromosomeSequenceMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    // partitioner is required to make sure all fragments from a given chromosome go to the same reducers
    job.setPartitionerClass(FragmentPartitioner.class);

    job.setReducerClass(ChromosomeSequenceReducer.class);
    job.setNumReduceTasks(1); // one reducer for each chr?
    job.setOutputFormatClass(FASTAOutputFormat.class);

    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 70);
    FASTAOutputFormat.addHeader(job, new Path(output, chr),
                                new FASTAHeader("chr" + chr, genomeName, "parent=" + parent, "hbase-generation"));

    return job;
    }


  @Override
  public int run(String[] args) throws Exception
    {
    /* Set up job */
    Job job = this.createJob(args);
    return (job.waitForCompletion(true) ? 0 : 1);
    }


  }
