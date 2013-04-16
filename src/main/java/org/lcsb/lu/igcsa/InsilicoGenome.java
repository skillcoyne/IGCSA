package org.lcsb.lu.igcsa;


import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.FileUtils;

import org.lcsb.lu.igcsa.variation.Variation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
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
  private ApplicationContext context;

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
    String genomeName = String.valueOf(new Random().nextInt(Math.abs((int) (time))));
    boolean overwriteGenome = false;

    if (cl.hasOption('n') || cl.hasOption("name")) genomeName = cl.getOptionValue('n');
    if (cl.hasOption('o') || cl.hasOption("overwrite")) overwriteGenome = true;

    List<String> chromosomes = new ArrayList<String>();
    if (cl.hasOption('c') || cl.hasOption("chromosome"))
      {
      for (String c: cl.getOptionValue('c').split(","))
        chromosomes.add(c);
      }

    File fastaDir = new File(genomeProperties.getProperty("dir.assembly"));
    if (chromosomes.size() > 0)
      {
      for (String c: chromosomes)
        genome.addChromosome( new Chromosome(c, FileUtils.getFASTA(c, fastaDir) ) );
      }
    else
      {
      genome.addChromosomes(FileUtils.getChromosomesFromFASTA(fastaDir));
      }
    log.info("Reference genome build: " + genome.getBuildName());
    log.info("Reference genome has: " + genome.getChromosomes().length + " chromosomes");

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
        chr.setVariantList( (List<Variation>) context.getBean("variantList") );
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
  Variable initialization. Most of it is done in the Spring configuration files.
   */
  private void init()
    {
    context = new ClassPathXmlApplicationContext(new String[] {"classpath*:spring-config.xml", "classpath*:/conf/genome.xml",
        "classpath*:/conf/database-config.xml"});
    //context = new ClassPathXmlApplicationContext("spring-config.xml");
    genome = (Genome) context.getBean("genome");
    genomeProperties = (Properties) context.getBean("genomeProperties");
    }

  private CommandLine parseCommandLine(String[] args)
    {
    Options options = new Options();
    options.addOption("n", "name", true, "Genome directory name, if not provided a random name is generated.");
    options.addOption("o", "overwrite", false, "Overwrite genome directory if name already exists.");
    options.addOption("t", "threads", true, "Number of concurrent threads, default is 5.");
    options.addOption("c", "chromosome", true, "List of chromosomes, best used for debugging.");
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

    print("Running InsilicoGenome with the following parameters: ");
    for (Option opt: cl.getOptions())
      print( "\t-" + opt.getLongOpt() + " " + opt.getValue("true"));

    return cl;
    }

  }
