package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.IGCSATables;
import org.lcsb.lu.igcsa.mapreduce.FragmentPartitioner;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.mapreduce.SegmentOrderComparator;
import org.lcsb.lu.igcsa.mapreduce.fasta.ChromosomeSequenceMapper;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;
import org.lcsb.lu.igcsa.mapreduce.fasta.SingleChromosomeReducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
  private boolean wait = false;
  private Job job;

  public NormalChromosomeJob(Configuration conf)
    {
    super(conf);

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

  public NormalChromosomeJob(Configuration conf, boolean wait)
    {
    this(conf);
    this.wait = wait;
    }

    private Scan setup(String[] args) throws IOException, ParseException
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());

    genomeName = cl.getOptionValue("g");
    chr = cl.getOptionValue("c");

    output = new Path(new Path(new Path(cl.getOptionValue("o"), Paths.GENOMES.getPath()), genomeName), chr);
    this.checkPath(output, true);

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    GenomeResult genome = admin.getGenomeTable().getGenome(genomeName);
    if (genome == null)
      throw new IOException("No genome found for " + genomeName + ". Exiting.");
    else
      parent = genome.getParent();

    // ONLY works in 0.94 and above, but AWS uses 0.92
//    char c = SequenceRow.initialChar(chr);
//    String rowKey = c + "???????????:" + chr + "-" + genomeName;
//    List<Pair<byte[], byte[]>> fuzzyKeys = new ArrayList<Pair<byte[], byte[]>>();
//    fuzzyKeys.add(new Pair<byte[], byte[]>(Bytes.toBytes(rowKey), new byte[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
//    scan.setFilter(new FuzzyRowFilter(fuzzyKeys));

    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL,
        Bytes.toBytes(genomeName)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL,
        Bytes.toBytes(chr)));

    Scan scan = new Scan();
    scan.setFilter(filters);
    scan.setCaching(100);

    return scan;
    }

  public void createJob(String[] args) throws IOException, ParseException
    {
    Scan scan = setup(args);
    /* Set up job */
    Job job = new Job(getConf(), "Generate FASTA file for chromosome " + chr + " in " + genomeName);
    job.setJarByClass(NormalChromosomeJob.class);

    job.setMapperClass(ChromosomeSequenceMapper.class);
    ChromosomeSequenceMapper.setChromosomes(job, new String[]{chr});

    TableMapReduceUtil.initTableMapperJob(IGCSATables.SEQ.getTableName(), scan, ChromosomeSequenceMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    // partitioner is required to make sure all fragments from a given chromosome go to the same reducers
    job.setPartitionerClass(FragmentPartitioner.class);

    job.setReducerClass(SingleChromosomeReducer.class);
    job.setNumReduceTasks(1); // one reducer for each chr?
    job.setOutputFormatClass(FASTAOutputFormat.class);

    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 70);
    FASTAOutputFormat.addHeader(job, new FASTAHeader("chr" + chr, genomeName, "parent=" + parent, "hbase-generation"));

    this.job = job;
    }

  public Job getJob()
    {
    return this.job;
    }



  @Override
  public int run(String[] args) throws Exception
    {
    /* Set up job */
    this.createJob(args);
    return (job.waitForCompletion(wait) ? 0 : 1);
    }

  public void renameToFASTA() throws IOException
    {
    FileSystem fs = this.getJobFileSystem(this.output.toUri());
    FileUtil.copy(fs, new Path(this.output, "part"), fs, new Path(this.output.toString() + ".fa"), true, this.getConf());
    fs.delete(this.output, true);
    }


  public static void main(String[] args) throws Exception
    {
    NormalChromosomeJob chr = new NormalChromosomeJob(new Configuration());

    chr.createJob(new String[]{"-g", "GRCh37", "-c", "Y", "-o", "/tmp"});
    Job job = chr.getJob();
    job.waitForCompletion(true);

    chr.renameToFASTA();
    }


  }
