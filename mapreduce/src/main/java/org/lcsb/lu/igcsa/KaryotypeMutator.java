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
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.KaryotypeGenerator;
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.lcsb.lu.igcsa.mapreduce.FASTAFragmentInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FASTAOutputFormat;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class KaryotypeMutator extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(KaryotypeMutator.class.getName());
  static ClassPathXmlApplicationContext springContext;

  static List<Aberration> aberrations;

  public static class KaryotypeMapper extends Mapper<LongWritable, Text, LongWritable, Text>
    {
    private static Logger log = Logger.getLogger(KaryotypeMapper.class.getName());

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
      {


      super.map(key, value, context);
      }
    }

  public static class KaryotypeReducer extends Reducer<LongWritable, Text, LongWritable, Text>
    {
    private static Logger log = Logger.getLogger(KaryotypeReducer.class.getName());

    @Override
    protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
      {
      super.reduce(key, values, context);
      }
    }


  public KaryotypeMutator()
    {
    springContext = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml"});
    }

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
    Properties props = (Properties) springContext.getBean("genomeProperties");

    // Translocation
    //1(p13),107200001-117800000
    //22(q11),14700001-25900000


//    KaryotypeGenerator karyotypeGenerator = (KaryotypeGenerator) springContext.getBean("karyotypeGenerator");
//    Karyotype karyotype = new KaryotypeInsilicoGenome(springContext, null).getGenome();
//    karyotype = karyotypeGenerator.generateKaryotypes(karyotype);
//    while (karyotype.getAberrationDefinitions().size() <= 0)
//      karyotype = karyotypeGenerator.generateKaryotypes(karyotype);
//
//    aberrations = karyotype.getAberrationDefinitions();

    String[] options = new GenericOptionsParser(conf, args).getRemainingArgs();
//    if (options.length <= 0)
//      throw new Exception("Karyotype description file required");

    String ktDescPath = "/tmp/test-karyotype.props";// options[0];
    conf.set("karyotype", ktDescPath);

    Properties ktProps = new Properties();
    ktProps.load( new FileInputStream(new File(ktDescPath)) );

    String[] chromosomes = ktProps.getProperty("derivatives").split(",");

    conf.set("kt", ktDescPath);

    //conf.addResource(ktDescPath);
    //conf.addResource(new FileInputStream(new File(ktDescPath)) );


    //CommandLine cl = parseCommandLine(options);

    //    String file = cl.getOptionValue('f');
    //    String chr = cl.getOptionValue('c');

    //    conf.set("chromosome", chr);
    //    conf.setInt("window", Integer.valueOf(props.getProperty("window")));

    //conf.set("karyotype", "/tmp/test-karyotype.props");
    //conf.addResource( new Path("/tmp/test-karyotype.props") );

    String outputPath = props.getProperty("dir.insilico");
    Job job = new Job(conf, "Fragment sequence mutation");
    job.setJarByClass(ChromosomeFragmentMutator.class);

    job.setInputFormatClass(FASTAInputFormat.class);
    job.setOutputFormatClass(FASTAOutputFormat.class);

    job.setMapperClass(KaryotypeMapper.class);
    job.setReducerClass(KaryotypeReducer.class);

    job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(Text.class);

    //FileInputFormat.addInputPath(job, new Path("/Users/sarah.killcoyne/Data/FASTA/"));

    for (String c: chromosomes)
      FileInputFormat.addInputPath(job, new Path("/Users/sarah.killcoyne/Data/FASTA/chr" + c + ".fa.gz"));

    FileUtils.deleteDirectory(new File("/tmp/kt-test"));
    FileOutputFormat.setOutputPath(job, new Path("/tmp/kt-test"));

    // Set only 1 reduce task
    //job.setNumReduceTasks(1);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new KaryotypeMutator(), args);
    }

  }
