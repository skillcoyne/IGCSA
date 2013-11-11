/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.ChromosomeBandDAO;
import org.lcsb.lu.igcsa.database.sql.JDBCChromosomeBandDAO;
import org.lcsb.lu.igcsa.mapreduce.FASTAFragmentInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FASTAOutputFormat;
import org.lcsb.lu.igcsa.mapreduce.job.Job;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class BandIndexer extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(BandIndexer.class.getName());

  static ClassPathXmlApplicationContext springContext;
  static ChromosomeBandDAO dao;

  public static class BandMapper extends Mapper<LongWritable, Text, LongWritable, Text>
    {
    private static Logger log = Logger.getLogger(BandMapper.class.getName());

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
      {
      super.map(key, value, context);
      }
    }

  public static class BandReducer extends Reducer<LongWritable, Text, LongWritable, Text>
    {
    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      }

    @Override
    protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
      {
      super.reduce(key, values, context);
      }
    }

  public BandIndexer()
    {
    springContext = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml"});
    if (springContext == null)
      throw new RuntimeException("Failed to load Spring application context");

    dao = (ChromosomeBandDAO) springContext.getBean("bandDAO");
    }

  @Override
  public int run(String[] args) throws Exception
    {
    int rj = 0;
    for (String c : new String[]{"19", "22"})
      {
      args = new String[]{c, "/Users/sarah.killcoyne/Data/FASTA/chr" + c + ".fa.gz"};

      final long startTime = System.currentTimeMillis();
      rj += runJob(getConf(), args);
      final long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("Finished job " + elapsedTime / 1000 + " seconds");
      }

    return rj;
    }


  public int runJob(Configuration conf, String[] args) throws Exception
    {
    String chr = args[0];
    String file = args[1];

    conf.set("chr", chr);

    String outputPath = "/tmp/Test/" + chr;

    Band[] bands = dao.getBands(chr);


    String[] strb = new String[bands.length];
    for (int i = 0; i < bands.length; i++)
      strb[i] = bands[i].getLocation().getStart() + "-" + bands[i].getLocation().getEnd();

    conf.setStrings("bands", strb);

    org.apache.hadoop.mapreduce.Job job = new org.apache.hadoop.mapreduce.Job(conf, "Chromosome indexer");
    job.setJarByClass(BandIndexer.class);

    job.setInputFormatClass(FASTAInputFormat.class);
    job.setOutputFormatClass(FASTAOutputFormat.class);

    job.setMapperClass(BandMapper.class);
    //job.setPartitionerClass(OrderedPartitioner.class);
    job.setReducerClass(BandReducer.class);

    job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(Text.class);

    FileInputFormat.addInputPath(job, new Path(file));

    FileUtils.deleteDirectory(new File(outputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    // one for each band
    job.setNumReduceTasks(bands.length);

    return (job.waitForCompletion(true) ? 0 : 1);

    }

  public static void main(String[] args) throws Exception
    {

    ToolRunner.run(new BandIndexer(), args);

    }


  }
