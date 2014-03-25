/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
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
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.mapreduce.*;
import org.lcsb.lu.igcsa.mapreduce.fasta.ChromosomeSequenceMapper;
import org.lcsb.lu.igcsa.mapreduce.fasta.ChromosomeSequenceReducer;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil;

import java.io.IOException;
import java.util.*;


/**
 * It might actually work best to run these as a set of separate jobs, one for each chromosome.
 */
public class GenerateFullGenome extends JobIGCSA
  {
  public static void main(String[] args) throws Exception
    {
    GenerateFullGenome gfg = new GenerateFullGenome();
    ToolRunner.run(gfg, args);

    // remove extraneous files and rename to .fa
    gfg.cleanUpFiles();
    // Create a single merged FASTA file for use in the indexing step
    Path mergedFasta = new Path(gfg.getOutputPath(), "reference.fa");
    FASTAUtil.mergeFASTAFiles(gfg.getJobFileSystem(), gfg.getOutputPath().toString(), mergedFasta.toString());

    // Run BWA index
    boolean runIndex = false;
    for (String arg : args)
      if (arg.equals("-b")) runIndex = true;

    if (runIndex)
      {
      Path tmp = BWAIndex.writeReferencePointerFile(mergedFasta, gfg.getJobFileSystem(mergedFasta.toUri()));
      ToolRunner.run(new BWAIndex(), (String[]) ArrayUtils.addAll(args, new String[]{"-f", tmp.toString()}));
      gfg.getJobFileSystem(tmp.toUri()).delete(tmp, true);
      }
    }


  private static final Log log = LogFactory.getLog(GenerateFullGenome.class);

  private Path output;
  private GenomeResult genome;
  private String genomeName;
  private List<String> chromosomes;

  public GenerateFullGenome()
    {
    super(new Configuration());

    Option genome = new Option("g", "genome", true, "Genome name.");
    genome.setRequired(true);
    this.addOptions(genome);

    Option output = new Option("o", "Output path", true, "Fully qualified path in HDFS or S3 to write FASTA files.");
    output.setRequired(true);
    this.addOptions(output);
    }

  public List<String> getChromosomes()
    {
    return chromosomes;
    }

  public Path getOutputPath()
    {
    return output;
    }

  protected void cleanUpFiles() throws IOException
    {
    FASTAUtil.deleteChecksumFiles(getJobFileSystem(), output);
    for (String c : chromosomes)
      FileUtil.copy(getJobFileSystem(), new Path(output, c), getJobFileSystem(), new Path(output, c + ".fa"), true, false, getConf());
    }

  private Scan setup() throws IOException
    {
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    genome = admin.getGenomeTable().getGenome(genomeName);

    Scan scan = admin.getSequenceTable().getScanFor(new Column("info", "genome", genomeName));
    scan.setCaching(100);

    chromosomes = new ArrayList<String>();
    for (ChromosomeResult chr : admin.getChromosomeTable().getChromosomesFor(genomeName))
      chromosomes.add(chr.getChrName());

    return scan;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());

    genomeName = cl.getOptionValue("g");
    output = new Path(new Path(cl.getOptionValue("o"), Paths.GENOMES.getPath()), genomeName);

    FileSystem fs = getJobFileSystem();
    if (output.toString().startsWith("s3")) fs = getJobFileSystem(output.toUri());

    if (fs.exists(output))
      {
      log.info("Overwriting output path " + output.toString());
      fs.delete(output, true);
      }

    Scan scan = setup();

    /* Set up job */
    Job job = new Job(getConf(), "Generate FASTA files for " + genomeName);
    job.setJarByClass(GenerateFullGenome.class);

    job.setMapperClass(ChromosomeSequenceMapper.class);
    ChromosomeSequenceMapper.setChromosomes(job, chromosomes.toArray(new String[chromosomes.size()]));

    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf()).getSequenceTable().getTableName(), scan, ChromosomeSequenceMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    // partitioner is required to make sure all fragments from a given chromosome go to the same reducers
    job.setPartitionerClass(FragmentPartitioner.class);

    job.setReducerClass(ChromosomeSequenceReducer.class);
    job.setNumReduceTasks(chromosomes.size()); // one reducer for each segment
    job.setOutputFormatClass(NullOutputFormat.class);

    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 70);
    for (String chr : chromosomes)
      {
      MultipleOutputs.addNamedOutput(job, chr, FASTAOutputFormat.class, LongWritable.class, Text.class);
      FASTAOutputFormat.addHeader(job, new Path(output, chr), new FASTAHeader("chr" + chr, genome.getName(),
                                                                              "parent=" + genome.getParent(), "hbase-generation"));
      }

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  }
