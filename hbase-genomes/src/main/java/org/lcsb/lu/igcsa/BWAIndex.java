/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.bwa.IndexMapper;

import java.io.*;


/*
Inputs:
-f reference genome text file (format, each line provides the path to a reference.fa file)
-b bwa tgz archive in hdfs
 */
public class BWAIndex extends BWAJob
  {
  private static final Log log = LogFactory.getLog(BWAIndex.class);

  private Path fastaTxt;

  public static void main(String[] args) throws Exception
    {
    if (args.length < 1)
      {
      System.err.println("Missing argument, <path to txt file>.");
      System.exit(-1);
      }

    ToolRunner.run(new BWAIndex(), args);
    }

  public static Path writeReferencePointerFile(Path mergedFasta, FileSystem fs) throws IOException
    {
    Path tmp = new Path(mergedFasta.getParent(), "ref.txt");

    FSDataOutputStream os = fs.create(tmp);
    os.write(mergedFasta.toString().getBytes());
    os.write("\n".getBytes());
    os.flush();
    os.close();

    return tmp;
    }

  public BWAIndex()
    {
    super(new Configuration());

    Option genome = new Option("f", "reference", true, "Reference genome text file.");
    genome.setRequired(true);
    this.addOptions(genome);
    }

  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());
    fastaTxt = new Path(cl.getOptionValue('f'));

    setupBWA(cl.getOptionValue('b'));

    Job job = new Job(getConf(), "BWA Index for " + fastaTxt.getParent().toString());
    job.setJarByClass(BWAIndex.class);

    job.setMapperClass(IndexMapper.class);
    job.setInputFormatClass(TextInputFormat.class);
    FileInputFormat.addInputPath(job, fastaTxt);

    job.setNumReduceTasks(0);
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  }

