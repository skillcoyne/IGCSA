package org.lcsb.lu.igcsa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.MultipleOutputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;
import java.util.Iterator;


/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

public class TestHadoop extends Configured implements Tool

  {
  static Logger log = Logger.getLogger(TestHadoop.class.getName());

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new TestHadoop(), args);
    }

  @Override
  public int run(String[] args) throws Exception
    {
    String InputFiles = args[0];
    String OutputDir = args[1];

    Configuration conf = new Configuration();
    Job job = new Job(conf, "Test");
    job.setJarByClass(TestHadoop.class);

    job.setOutputKeyClass(Text.class);
    job.setMapOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    job.setMapperClass(Map.class);
    job.setReducerClass(Reduce.class);

    job.setInputFormatClass(TextInputFormat.class);
    //job.setOutputFormatClass(MultiFileOutput.class);
    job.setOutputFormatClass(NullOutputFormat.class);

    FileInputFormat.setInputPaths(job, InputFiles);
    FileOutputFormat.setOutputPath(job, new Path(OutputDir));

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  static class Map extends Mapper<LongWritable, Text, Text, Text>
    {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
      {
      String[] dall = value.toString().split(":");
      context.write(new Text(dall[0]), new Text(dall[1]));
      }
    }

  static class Reduce extends Reducer<Text, Text, Text, Text>
    {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
      {
      Iterator<Text> tI = values.iterator();
      while (tI.hasNext()) context.write(key, tI.next());
      }
    }

  static class MultiFileOutput extends MultipleOutputFormat<Text, Text>
    {
    @Override
    protected RecordWriter<Text, Text> getBaseRecordWriter(FileSystem fileSystem, JobConf entries, String s, Progressable progressable) throws IOException
      {
      return null;
      }

    protected String generateFileNameForKeyValue(Text key, Text value, String name)
      {
      return key.toString();
      }
    }

  }
