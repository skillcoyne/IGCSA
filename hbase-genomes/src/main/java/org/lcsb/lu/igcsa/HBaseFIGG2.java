package org.lcsb.lu.igcsa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.FragmentDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.mapreduce.FASTAFragmentInputFormat;
import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Random;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class HBaseFIGG2 extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(HBaseFIGG2.class.getName());

  @Override
  public int run(String[] args) throws Exception
    {
    final long startTime = System.currentTimeMillis();
    int rj = runJob(getConf(), args);
    final long elapsedTime = System.currentTimeMillis() - startTime;
    log.info("Finished job " + elapsedTime / 1000 + " seconds");

    return rj;
    }

  private int runJob(Configuration conf, String[] args) throws Exception
    {
    //    ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml",
    // "classpath*:/conf/genome.xml"});

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    config.set("genome", "GRCh37");
    config.set("chr", "22");

    Job job = new Job(config, "ExampleReadWrite");

    job.setJarByClass(HBaseFIGG2.class);

    job.setInputFormatClass(FASTAFragmentInputFormat.class);

    FileInputFormat.addInputPath(job, new Path("/Users/skillcoyne/Data/FASTA/chr22.fa.gz"));
    FileOutputFormat.setOutputPath(job, new Path("/tmp/figg2"));

    job.setMapperClass(FragmentMapper.class);
    //job.setReducerClass(FragmentReducer.class);

    TableMapReduceUtil.initTableReducerJob(genomeAdmin.getGenomeTable().getTableName(),        // output table
                                           FragmentReducer.class,    // reducer class
                                           job);

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  public static class FragmentMapper extends Mapper<ImmutableBytesWritable, Text, ImmutableBytesWritable, Text>
    {
    private static Logger log = Logger.getLogger(FragmentMapper.class.getName());

    private String genomeName;
    private String chr;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      chr = context.getConfiguration().get("chromosome");
      }

    @Override
    protected void map(ImmutableBytesWritable key, Text value, Context context) throws IOException, InterruptedException
      {
      //      // pretty much just chopping the file up and spitting it back out
      //      context.write(key, value);

      super.map(key, value, context);
      }

    //    @Override
//    protected void map(ImmutableBytesWritable key, Text value, Context context) throws IOException, InterruptedException
//      {
//      // pretty much just chopping the file up and spitting it back out
//      context.write(key, value);
//      }
    }


  public static class FragmentReducer extends TableReducer<ImmutableBytesWritable, Text, ImmutableBytesWritable>
    {
    private String chr;
    private String genomeName;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);

      genomeName = context.getConfiguration().get("genome");
      chr = context.getConfiguration().get("chromosome");

      //tables = (MutableGenome) springContext.getBean("tables");

      log.info("CHROMOSOME " + chr);
      }

    @Override
    public void reduce(ImmutableBytesWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
      {
      Text result = new Text();
      /*
      so I don't understand what's going on here.  Each mapper should have only one value per key but if I iterate over the values I get
      each value duplicated for each key.
      for now I just won't iterate since I'm missing something
      */
      result.set(values.iterator().next().toString());
      //context.write(key, result);
      }
    }


  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new HBaseFIGG2(), args);
    }


  }
