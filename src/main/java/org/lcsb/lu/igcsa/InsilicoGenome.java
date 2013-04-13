package org.lcsb.lu.igcsa;


import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.FileUtils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * org.lcsb.lu.igcsa
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class InsilicoGenome
  {
  static Logger log = Logger.getLogger(InsilicoGenome.class.getName());

  protected Properties genomeProperties;

  // Defaults
  private int windowSize = 1000;

  protected Genome genome;

  private ExecutorService executorService;

  protected void print(String s)
    {
    System.out.println(s);
    }

  public static void main(String[] args) throws Exception
    {
    InsilicoGenome igAp = new InsilicoGenome(args);
    }


  public InsilicoGenome(String[] args) throws Exception
    {
    init();
    CommandLine cl = parseCommandLine(args);

    long time = java.lang.System.currentTimeMillis();
    log.debug("" + time);
    String genomeName = String.valueOf(new Random().nextInt(Math.abs((int) (time))));
    boolean overwriteGenome = false;

    if (cl.hasOption('n') || cl.hasOption("name")) genomeName = cl.getOptionValue('n');
    if (cl.hasOption('o') || cl.hasOption("overwrite")) overwriteGenome = true;

    log.debug(genomeName + " " + overwriteGenome);

    genomeSetup();

    int threads = 5;
    if (cl.hasOption('t') || cl.hasOption("threads")) threads = Integer.valueOf(cl.getOptionValue('t'));

    executorService = Executors.newFixedThreadPool(threads);

    createGenome(genomeName, overwriteGenome);

    log.info("Finished creating genome " + genomeName);
    print("Finished creating genome " + genomeName);
    }


  public void createGenome(String name, boolean overwrite) throws IOException
    {
    File genomeDirectory = new File(genomeProperties.getProperty("dir.insilico"), name);

    if (genomeDirectory.exists() && overwrite)
      {
      log.warn("Overwriting " + genomeDirectory.getAbsolutePath());
      for (File f : genomeDirectory.listFiles()) f.delete();
      genomeDirectory.delete();
      }
    else if (genomeDirectory.exists() && !overwrite)
      throw new IOException(genomeDirectory + " exists, cannot overwrite");

    log.info(genomeDirectory.getAbsolutePath());

    for (Chromosome chr : genome.getChromosomes()) //this could be done in threads, each chromosome can be mutated separately
      {
      log.info(chr.getName());
      try
        {
        FASTAHeader header = new FASTAHeader(">chromosome|" + chr.getName() + "|individual " + name);
        FASTAWriter writer = new FASTAWriter(new File(genomeDirectory, "chr" + chr.getName() + ".fa"), header);
        executorService.execute(genome.mutate(chr, windowSize, writer));
        }
      catch (IOException e)
        {
        e.printStackTrace();
        }
      }
    executorService.shutdown();
    }


  /*
   * Sets up the reference genome based on the fasta files for the current build.
   */
  protected void genomeSetup() throws FileNotFoundException, ProbabilityException, IllegalAccessException, InstantiationException
    {
    genome.addChromosomes(FileUtils.getChromosomesFromFASTA(new File(genomeProperties.getProperty("dir.assembly"))));
    log.info("Reference genome build: " + genome.getBuildName());
    log.info("Reference genome has: " + genome.getChromosomes().length + " chromosomes");
    }


  /*
  Variable initialization. Most of it is done in the Spring configuration files.
   */
  private void init()
    {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    genome = (Genome) context.getBean("genome");
    genomeProperties = (Properties) context.getBean("genomeProperties");
    }

  private CommandLine parseCommandLine(String[] args)
    {
    Options options = new Options();
    options.addOption("n", "name", true, "Genome directory name, if not provided a random name is generated.");
    options.addOption("o", "overwrite", false, "Overwrite genome directory if name already exists.");
    options.addOption("t", "threads", true, "Number of concurrent threads, default is 5.");
    //options.addOption("v", "version", false, "");
    options.addOption("h", "help", false, "print usage help");

    CommandLineParser clp = new BasicParser();
    CommandLine cl = null;
    try
      {
      cl = clp.parse(options, args);
      if (cl.hasOption('h') || cl.hasOption("help"))
        {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("<jar file>", options);
        System.exit(0);
        }
      }
    catch (ParseException e)
      {
      log.error(e);
      }
    return cl;
    }

  }
