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
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.*;

import java.io.IOException;
import java.util.Iterator;


public class Test  extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(Test.class);

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new Test(), args);
    }

  public int run(String[] strings) throws Exception
    {
    Configuration conf = new Configuration();
    Job job = new Job(conf, "testing");
    job.setJarByClass(Test.class);

    job.setMapperClass(TestMapper.class);
    job.setInputFormatClass(WholeFileInputFormat.class);
    WholeFileInputFormat.addInputPath(job, new Path("hdfs://localhost:9000/tmp/igcsa/testsam"));

    job.setReducerClass(TestReducer.class);
    job.setNumReduceTasks(1);
    job.setOutputFormatClass(NullOutputFormat.class);
    FileOutputFormat.setOutputPath(job, new Path("hdfs://localhost:9000/tmp/testbwa"));

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  static class TestMapper extends Mapper<NullWritable, BytesWritable, NullWritable, BytesWritable>
    {
    protected void map(NullWritable key, BytesWritable value, Context context) throws IOException, InterruptedException
      {
      log.info(value.getLength());
      super.map(key, value, context);
      }
    }

  static class TestReducer extends Reducer<NullWritable, BytesWritable, NullWritable, BytesWritable>
    {
    protected void reduce(NullWritable key, Iterable<BytesWritable> values, Context context) throws IOException, InterruptedException
      {
      Iterator<BytesWritable> bI = values.iterator();
      while (bI.hasNext())
        {
        BytesWritable bw = bI.next();

        }


      }
    }

  }


