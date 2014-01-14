/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.NullReducer;

import java.io.IOException;


public class BWAIndex extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(BWAIndex.class);

  private Configuration conf;
  private Path fasta;


  public static void main(String[] args) throws Exception
    {
    new BWAIndex().run(args);
    }

  public int run(String[] args) throws Exception
    {
    this.fasta = new Path(args[0]);
    this.conf = new Configuration();

    Job job = new Job(conf, "Generate normal FASTA files");
    job.setJarByClass(GenerateDerivativeChromosomes.class);

    job.setMapperClass(FASTAMapper.class);
    job.setReducerClass(NullReducer.class);

    job.setInputFormatClass(FASTAInputFormat.class);
    FileInputFormat.addInputPath(job, fasta);

    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  static class FASTAMapper extends Mapper<Text, Text, Text, Text>
    {

    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException
      {
      log.info(key);
      log.info(value);

      super.map(key, value, context);
      }
    }


  }
