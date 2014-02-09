package org.lcsb.lu.igcsa;

import com.m6d.filecrush.crush.Crush;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.mapreduce.NullMapper;
import org.lcsb.lu.igcsa.mapreduce.NullReducer;
import org.lcsb.lu.igcsa.mapreduce.bwa.*;

import java.io.*;
import java.net.URI;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class BWAAlign extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(BWAAlign.class.getName());

  private File[] fastqPair;
  private FileSystem jobFS;


  public BWAAlign(File[] fastqFiles) throws Exception
    {
    fastqPair = fastqFiles;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    args = new String[]{"/tmp/igcsa/reads", "hdfs://localhost:9000/tmp/test6"};

    Path output = new Path(args[0]);
    Path reference = new Path(args[1], "index.tgz");

    Configuration conf = new Configuration();

    conf.set("dfs.block.size", "16777216");
    conf.set("mapred.text.key.partitioner.options", "-k1,4");
    conf.set("mapred.text.key.comparator.options", "-k1,4");
    conf.set("mapred.output.key.comparator.class", "org.apache.hadoop.mapreduce.lib.partition.KeyFieldBasedComparator");

    URI uri = new URI("hdfs://localhost:9000/bwa-tools/bwa.tgz#tools");

    // WARNING: DistributedCache is not accessible on local runner (IDE) mode.  Has to run as a hadoop job to test
    // bwa tools
    DistributedCache.addCacheArchive(uri, conf);
    DistributedCache.createSymlink(conf);

    // reference
    if (!reference.getFileSystem(conf).exists(reference))
      throw new RuntimeException("Reference index " + reference.toString() + " does not exist");
    uri = new URI(reference.toUri().toASCIIString() + "#reference");
    DistributedCache.addCacheArchive(uri, conf);
    DistributedCache.createSymlink(conf);

    // set up the job
    Job job = new Job(conf, "Align read pairs.");

    Path tsvInput = new FastqToTSV(fastqPair[0], fastqPair[1]).toTSV(output.getFileSystem(conf), output);
    job.setJarByClass(BWAAlign.class);
    job.setMapperClass(ReadPairMapper.class);

    job.setInputFormatClass(ReadPairTSVInputFormat.class);
    TextInputFormat.addInputPath(job, tsvInput);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);

    // it's possible this might work on an actual cluster however, in testing on a single node functionally this hangs the job
    // since what's really needed is to just cat the files together a separate job run after this one can function
    job.setReducerClass(SAMReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setOutputFormatClass(SAMOutputFormat.class);

    Path outputPath = new Path("/tmp/igcsa/sam");
    if (outputPath.getFileSystem(conf).exists(outputPath)) outputPath.getFileSystem(conf).delete(outputPath, true);

    FileOutputFormat.setOutputPath(job, outputPath);

    this.jobFS = outputPath.getFileSystem(conf);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public FileSystem getJobFS()
    {
    return jobFS;
    }
  // HEADER IS STILL OUTPUTTING MULTIPLE TIMES....

  public static void main(String[] args) throws Exception
    {
    args = new String[]{"/Users/skillcoyne/Data/1000Genomes/Reads/ERX000272/unzipped"};
    if (args.length < 1)
      {
      System.err.println("Missing argument for local path of read pair fastq files.");
      System.exit(-1);
      }

    String readPairDir = args[0];
    File dir = new File(readPairDir);
    if (!dir.isDirectory() || !dir.canRead()) throw new IOException(readPairDir + " is not a directory or is unreadable.");

    File[] fastqFiles = dir.listFiles(new FilenameFilter()
    {
    @Override
    public boolean accept(File file, String name)
      {
      return (name.endsWith(".fastq") || name.endsWith(".fastq.gz"));
      }
    });

    if (fastqFiles.length > 2)
      throw new IOException(dir + " contains more than 2 fastq files. Please ensure directory contains only read-pair files.");


    BWAAlign align = new BWAAlign(fastqFiles);
    ToolRunner.run(align, null);

    for (FileStatus status: align.getJobFS().listStatus(new Path("/tmp/igcsa/sam")))
      {
      if (status.getLen() <= 0)
        align.getJobFS().delete(status.getPath(), true);
      }

    // Merge the files into a single SAM
    ToolRunner.run(new Crush(), new String[]{"--input-format=text", "--output-format=text", "--compress=none", "/tmp/igcsa/sam",
        "/tmp/igcsa/test.sam"});

    }


  }
