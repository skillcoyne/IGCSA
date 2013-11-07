/**
 * org.lu.igcsa.hadoop.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hadoop.mapreduce;


import org.apache.commons.cli.*;
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
import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.genome.MutableGenome;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.lcsb.lu.igcsa.variation.fragment.Variation;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class SequenceRead extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(SequenceRead.class.getName());

  static ClassPathXmlApplicationContext springContext;


  @Override
  public int run(String[] args) throws Exception
    {
    final long startTime = System.currentTimeMillis();
    int rj = runJob(getConf(), args);
    final long elapsedTime = System.currentTimeMillis() - startTime;
    log.info("Finished job " + elapsedTime/1000 + " seconds");

    return rj;
    }

  public static class FragmentMapper extends Mapper<LongWritable, Text, LongWritable, Text>
    {
    private static Logger log = Logger.getLogger(FragmentMapper.class.getName());

    private VariantUtils variantUtils;
    private String chr;
    // Database connections
    private GCBinDAO binDAO;
    private FragmentDAO fragmentDAO;


    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      variantUtils = (VariantUtils) springContext.getBean("variantUtils");
      chr = context.getConfiguration().get("chromosome");
      binDAO = (GCBinDAO) springContext.getBean("GCBinDAO");
      fragmentDAO = (FragmentDAO) springContext.getBean("FragmentDAO");
      }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
      {
      final long startTime = System.currentTimeMillis();

      // I think this is where I would process the sequence
      Random randomFragment = new Random();
      DNASequence sequence = new DNASequence(value.toString());

      // Process the sequence, mutate and return the mutated sequence
      if (sequence.calculateNucleotides() > (0.3 * sequence.getLength())) //unknown sequence makes up less than 30%
        {
        // get random fragment within the GC bin for each variation
        Bin gcBin = this.binDAO.getBinByGC(chr, sequence.calculateGC()); // TODO some of these calls take > 20ms (not even most though)
        Map<String, Fragment> fragmentVarMap = fragmentDAO.getFragment(chr, gcBin.getBinId(), randomFragment.nextInt(gcBin.getSize()));

        try
          {
          // mutate the sequence
          for (Variation variation : variantUtils.getVariantList(chr))
            {
            Fragment fragment = fragmentVarMap.get(variation.getVariationName());
            log.debug("Chromosome " + chr + " MUTATING FRAGMENT " + fragment.toString());
            variation.setMutationFragment(fragment);
            sequence = variation.mutateSequence(sequence);

            // I want to keep a log of every mutation so I think for the M/R classes the value can be an array of Text objects...
            //writeVariations(chr, location, gcBin, variation, variation.getLastMutations());
            }
          value = new Text(sequence.toString());
          }
        catch (Exception e)
          {
          e.printStackTrace();
          }
        }
      context.write(key, value);

      final long elapsedTime = System.currentTimeMillis() - startTime;
      log.info(key + " finished map " + elapsedTime + " ms");
      }
    }

  public static class LongReducer extends Reducer<LongWritable, Text, LongWritable, Text>
    {
    private Genome genome;
    private String chr;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      chr = context.getConfiguration().get("chromosome");

      //genome = (MutableGenome) springContext.getBean("genome");

      log.info("CHROMOSOME " + chr);
      }

    @Override
    public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
      {
      Text result = new Text();
      /*
      so I don't understand what's going on here.  Each mapper should have only one value per key but if I iterate over the values I get each value duplicated for each key.
      for now I just won't iterate since I'm missing something
      */

      result.set(values.iterator().next().toString());
      context.write(key, result);
      }
    }


  public SequenceRead()
    {
    springContext = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml"});


    Properties genomeProperties = (Properties) springContext.getBean("genomeProperties");

    if (genomeProperties.containsKey("window"))
      {
      int windowSize = Integer.valueOf(genomeProperties.getProperty("window"));
      log.info("window " + windowSize);
      }
    else
      {
      throw new RuntimeException("Property 'window' not found.");
      }


    }

  // only running a single chromosome at the moment
  public int runJob(Configuration conf, String[] args) throws Exception
    {
    String[] options = new GenericOptionsParser(conf, args).getRemainingArgs();
    CommandLine cl = parseCommandLine(options);

    // TODO only dealing with one at the moment
    String[] files = cl.getOptionValue('c').split(",");
    String[] chrFile = files[0].split(":");

    if (chrFile.length < 2)
      throw new Exception("Chromosome files should be provided in the following format:  <chromosome name>:<path to fasta file>");

    String outputPath = cl.getOptionValue('o', "/tmp/seq-hadoop-test");
    outputPath = outputPath + "/" + chrFile[0];

    conf.set("chromosome", chrFile[0]);
    Job job = new Job(conf, "Fragment sequence mutation");
    job.setJarByClass(SequenceRead.class);

    job.setInputFormatClass(FASTAFragmentInputFormat.class);
    job.setOutputFormatClass(FASTAOutputFormat.class);

    job.setMapperClass(FragmentMapper.class);
    job.setReducerClass(LongReducer.class);

    job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(Text.class);

    FileInputFormat.addInputPath(job, new Path(chrFile[1]));
    //FileInputFormat.addInputPath(job, new Path("/Users/sarah.killcoyne/Dropbox/Private/Work/fasta-hadoop/src/test/resources/hadoop-test.txt"));

    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    // Set only 1 reduce task
    //job.setNumReduceTasks(1);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  private CommandLine parseCommandLine(String[] args)
    {
    Options options = new Options();
    options.addOption("c", "chromosomes", true, "List of chromosomes and file locations, comma separated. (ex. 22:/path/to/22.fa.gz, X:/path/to/X.fa.gz");

    options.addOption("h", "help", false, "print usage help");

    CommandLineParser clp = new BasicParser();
    CommandLine cl = null;
    try
      {
      cl = clp.parse(options, args);
      HelpFormatter help = new HelpFormatter();
      if (cl.hasOption('h') || cl.hasOption("help"))
        {
        help.printHelp("<jar file>", options);
        System.exit(0);
        }
      }
    catch (ParseException e)
      {
      e.printStackTrace();
      }
    return cl;
    }

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new SequenceRead(), args);
    }


  }