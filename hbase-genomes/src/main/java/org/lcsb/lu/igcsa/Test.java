///**
// * org.lcsb.lu.igcsa
// * Author: sarah.killcoyne
// * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
// * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
// */
//
//
//package org.lcsb.lu.igcsa;
//
//import fi.tkk.ics.hadoop.bam.cli.Frontend;
//import net.sf.picard.sam.MergeSamFiles;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.conf.Configured;
//import org.apache.hadoop.filecache.DistributedCache;
//import org.apache.hadoop.fs.FSDataInputStream;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.*;
//import org.apache.hadoop.mapreduce.*;
//import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
//import org.apache.hadoop.mapreduce.lib.input.FileSplit;
//import org.apache.hadoop.mapreduce.lib.input.KeyValueLineRecordReader;
//import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
//import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
//import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
//import org.apache.hadoop.util.StringUtils;
//import org.apache.hadoop.util.Tool;
//import org.apache.hadoop.util.ToolRunner;
//import org.lcsb.lu.igcsa.mapreduce.*;
//import org.lcsb.lu.igcsa.mapreduce.fasta.CharacterReader;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//
//public class Test extends Configured implements Tool
//  {
//  private static final Log log = LogFactory.getLog(Test.class);
//
//  public static void main(String[] args) throws Exception
//    {
//    ToolRunner.run(new Test(), args);
//    }
//
//  public int run(String[] strings) throws Exception
//    {
//    // sam to bam using samtools
//    Configuration conf = new Configuration();
//
//    URI uri = new URI("hdfs://localhost:9000/bwa-tools/bwa.tgz#tools");
//
//    // WARNING: DistributedCache is not accessible on local runner (IDE) mode.  Has to run as a hadoop job to test
//    DistributedCache.addCacheArchive(uri, conf);
//    DistributedCache.createSymlink(conf);
//
//
//    Job job = new Job(conf, "testing");
//    job.setJarByClass(Test.class);
//
//    job.setMapperClass(TestMapper.class);
//    job.setInputFormatClass(SAMFileInputFormat.class);
//    //job.setInputFormatClass(WholeFileInputFormat.class);
//    job.setMapOutputKeyClass(Text.class);
//    job.setMapOutputValueClass(Text.class);
//    FileInputFormat.addInputPath(job, new Path("hdfs://localhost:9000/tmp/igcsa/sam/part-00007"));
//
//    job.setReducerClass(TestReducer.class);
//    job.setNumReduceTasks(1);
//    job.setOutputFormatClass(NullOutputFormat.class);
//
//    FileOutputFormat.setOutputPath(job, new Path("hdfs://localhost:9000/tmp/javabwa"));
//
//    return (job.waitForCompletion(true) ? 0 : 1);
//    }
//
//
//  static class TestMapper extends Mapper<Text, Text, Text, Text>
//    {
//    @Override
//    protected void setup(Context context) throws IOException, InterruptedException
//      {
//      Path[] localArchives = DistributedCache.getLocalCacheArchives(context.getConfiguration());
//
//      File bwaBinary = new File("tools/bwa");
//      if (!bwaBinary.exists())
//        throw new RuntimeException("bwa binary does not exist in the cache.");
//
//      log.info("BWA BINARY FOUND: " + bwaBinary);
//      super.setup(context);
//      }
//
//
//    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException
//      {
////      log.info("key:" + key);
////      log.info(value);
//
//      super.map(key, value, context);
//      }
//    }
//
//  static class TestReducer extends Reducer<Text, Text, Text, Text>
//    {
//
//    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
//      {
//      Iterator<Text> tI = values.iterator();
//      while (tI.hasNext())
//        {
//        Text t = tI.next();
//        //log.info(t);
//        }
//
//      Process p = Runtime.getRuntime().exec("tools/bwa index");
//
//      BWAIndex.StreamWrapper error, output;
//      error = BWAIndex.getStreamWrapper(p.getErrorStream(), "ERROR");
//      output = BWAIndex.getStreamWrapper(p.getInputStream(), "OUTPUT");
//      int exitVal = 0;
//
//      error.start();
//      output.start();
//      error.join(3000);
//      output.join(3000);
//      exitVal = p.waitFor();
//
//      log.info(exitVal);
//      log.info("BWA OUTPUT: " + output.message);
//      }
//    }
//
//  static class SAMFileInputFormat extends FileInputFormat<Text, Text>
//    {
//    @Override
//    protected boolean isSplitable(JobContext context, Path filename)
//      {
//      return false;
//      }
//
//    @Override
//    public RecordReader<Text, Text> createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException,
//    InterruptedException
//      {
//      RecordReader reader = new SAMRecordReader();
//      return reader;
//      }
//
//    static class SAMRecordReader extends RecordReader<Text, Text>
//      {
//      private StringBuffer samHeader;
//      private Text key;
//      private Text value;
//      private long splitStart;
//      private long splitEnd;
//      private FSDataInputStream inputStream;
//      private CharacterReader reader;
//
//      @Override
//      public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
//        {
//        FileSplit split = (FileSplit) inputSplit;
//        Path path = split.getPath();
//
//        splitStart = split.getStart();
//        splitEnd = splitStart + split.getLength();
//
//        inputStream = path.getFileSystem(context.getConfiguration()).open(path);
//        reader = new CharacterReader(inputStream);
//
//        samHeader = new StringBuffer();
//        }
//
//      @Override
//      public boolean nextKeyValue() throws IOException, InterruptedException
//        {
//        value = new Text();
//        String line;
//        while ((line = reader.readLine()) != null)
//          {
//          if (line.startsWith("@")) samHeader.append(line + "\n");
//          else value.append((line + "\n").getBytes(), 0, line.length() + 1);
//
//          key = new Text(samHeader.toString());
//          return true;
//          }
//        return false;
//        }
//
//      @Override
//      public Text getCurrentKey() throws IOException, InterruptedException
//        {
//        return key;
//        }
//
//      @Override
//      public Text getCurrentValue() throws IOException, InterruptedException
//        {
//        return value;
//        }
//
//      @Override
//      public float getProgress() throws IOException, InterruptedException
//        {
//        return inputStream.getPos() / splitEnd;
//        }
//
//      @Override
//      public void close() throws IOException
//        {
//        reader.close();
//        }
//      }
//
//    }
//
//  }
//
//
