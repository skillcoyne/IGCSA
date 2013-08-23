package org.lcsb.lu.igcsa;


import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.*;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.FileUtils;

import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


/**
 * org.lcsb.lu.igcsa
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FragmentInsilicoGenome
  {
  static Logger log = Logger.getLogger(FragmentInsilicoGenome.class.getName());

  public static VariantUtils variantUtils;

  protected Properties genomeProperties;
  protected MutableGenome genome;

  // Defaults
  private int windowSize = 1000;
  private int threads = 5;

  // Parallel execution
  private ExecutorService smallMutationExcecutor;

  // Spring
  private ApplicationContext context;

  public FragmentInsilicoGenome(ApplicationContext context, CommandLine cl) throws Exception
    {
    this.context = context;
    init();

    String genomeName = String.valueOf(new Random().nextInt(Math.abs((int) (System.currentTimeMillis()))));

    if (cl.hasOption('n'))
      genomeName = cl.getOptionValue('n');

    List<String> chromosomes = new ArrayList<String>();
    if (cl.hasOption('c') || cl.hasOption("chromosome"))
      { // get specific chromosome from command line if required
      for (String c : cl.getOptionValue('c').split(","))
        chromosomes.add(c);
      }
    setupChromosomes(chromosomes);

    // No idea if this is a smart way to do it. But this sets up the threading. No reason to have more threads than chromosomes.
    if (cl.hasOption('t') || cl.hasOption("threads"))
      threads = Integer.valueOf(cl.getOptionValue('t'));
    if (threads > genome.getChromosomes().length)
      threads = genome.getChromosomes().length;

    // set up the directory that the genome will write to
    setupGenomeDirectory(genomeName);
    log.info("Reference genome build: " + genome.getBuildName());
    }

  // Applies mutations first at the 1kb or less level then structural
  public void applyMutations() throws IOException, ExecutionException, InterruptedException
    {
    log.info("Applying mutations to " + genome.getBuildName());
    smallMutationExcecutor = Executors.newFixedThreadPool(threads);
    CompletionService taskPool = new ExecutorCompletionService<String>(smallMutationExcecutor);

    List<Future<Chromosome>> tasks = new ArrayList<Future<Chromosome>>();
    for (Chromosome chr : genome.getChromosomes()) //this could be done in threads, each chromosome can be mutated separately
      {
      log.debug(chr.getName());
      FASTAHeader header = new FASTAHeader("figg", "chr"+chr.getName(), "1kb mutations", genome.getBuildName());
      FASTAWriter writer = new FASTAWriter(new File(genome.getGenomeDirectory(), "chr" + chr.getName() + ".fa"), header);

      if (chr.getVariantList().size() <= 0) // this should never happen but...
        throw new RuntimeException("Missing variant list in chromosome " + chr.getName());

      Future<Chromosome> mutationF = taskPool.submit(genome.mutate(chr, windowSize, writer));
      tasks.add(mutationF);
      }

    for (int i = 0; i < tasks.size(); i++)
      {
      Future<Chromosome> f = taskPool.take();
      Chromosome c = f.get();
      // important to replace chromosome with one that will read it's recently created mutated FASTA file or else structural variations
      // will be applied to the original FASTA and all small mutations will be lost.
      Chromosome mc = new Chromosome(c.getName(), new File(genome.getGenomeDirectory(), "chr" + c.getName() + ".fa"));
      log.info(mc.getFASTA().getAbsolutePath());
      genome.replaceChromosome(mc);
      log.info("Small mutations finished on " + c.getName());
      }
    log.info("**** Small mutation step finished on " + tasks.size() + " chromosomes. ***");
    smallMutationExcecutor.shutdown();
    }


  // Create the directory where all fasta and mutation files will write
  private void setupGenomeDirectory(String name) throws IOException
    {
    File genomeDirectory = new File(genomeProperties.getProperty("dir.insilico"), name);

    if (!genomeDirectory.exists())
      throw new IllegalArgumentException(genomeDirectory.getAbsolutePath() + " does not exist.");

    genome.setGenomeDirectory(genomeDirectory);
    genome.setBuildName(name);

    File mutWriterPath = new File(genomeDirectory, "fragment-mutations");
    if (!mutWriterPath.exists() || !mutWriterPath.isDirectory()) mutWriterPath.mkdir();

    genome.setMutationDirectory(mutWriterPath);
    }

  // Adds chromosomes to the genome.  Either from the command line, or from the assembly directory
  private void setupChromosomes(List<String> chromosomes) throws IOException, ProbabilityException, InstantiationException, IllegalAccessException
    {
    // Set up the chromosomes in the genome that will be mutated.
    File fastaDir = new File(genomeProperties.getProperty("dir.assembly"));
    log.info("Reading FASTA directory " + fastaDir.getAbsolutePath());
    if (!fastaDir.exists() || !fastaDir.canRead())
      throw new IOException("FASTA directory does not exist or is not readable: " + fastaDir.getAbsolutePath());


    List<Chromosome> chromosomeList = new ArrayList<Chromosome>();
    if (chromosomes.size() > 0)
      {
      for (String c : chromosomes)
        chromosomeList.add(new Chromosome(c, FileUtils.getFASTA(c, fastaDir)));
      }
    else
      chromosomeList = FileUtils.getChromosomesFromFASTA(fastaDir);

    for (Chromosome chr: chromosomeList)
      {
      chr.setVariantList(variantUtils.getVariantList(chr.getName()));
      genome.addChromosome(chr);
      }

    log.info("Reference genome has: " + genome.getChromosomes().length + " chromosomes");
    }

  // Variable initialization. Most of it is done in the Spring configuration files.
  private void init() throws Exception
    {
    // be nice to autowire this so I don't have to make calls into Spring but not important for now
    genome = (MutableGenome) context.getBean("genome");
    variantUtils = (VariantUtils) context.getBean("variantUtils");
    genomeProperties = (Properties) context.getBean("genomeProperties");

    if (genomeProperties.containsKey("window"))
      {
      windowSize = Integer.valueOf( genomeProperties.getProperty("window") );
      }
    else
      {
      throw new Exception("Property 'window' not found.");
      }

    }

  }
