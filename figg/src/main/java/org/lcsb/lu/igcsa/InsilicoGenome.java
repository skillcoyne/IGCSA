package org.lcsb.lu.igcsa;


import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.genome.Location;
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
public class InsilicoGenome
  {
  static Logger log = Logger.getLogger(InsilicoGenome.class.getName());

  public static VariantUtils variantUtils;

  protected Properties genomeProperties;
  protected Genome genome;

  // Defaults
  private int windowSize = 1000;
  private int threads = 5;
  private boolean applySV = false;
  private boolean applySM = false;

  // Parallel execution
  private ExecutorService smallMutationExcecutor;
  private ExecutorService structuralVariationExecutor;

  // Spring
  private ApplicationContext context;

  public static void main(String[] args) throws Exception
    {
    final long startTime = System.currentTimeMillis();

    InsilicoGenome igAp = new InsilicoGenome(args);
    // Apply mutations!
    igAp.applyMutations();

    final long elapsedTimeMillis = System.currentTimeMillis() - startTime;
    log.info("FINISHED mutating genome " + igAp.genome.getBuildName());
    log.info("Elapsed time (seconds): " + elapsedTimeMillis / 1000);
    }

  public InsilicoGenome(String[] args) throws Exception
    {
    init();
    CommandLine cl = parseCommandLine(args);

    String genomeName = String.valueOf(new Random().nextInt(Math.abs((int) (System.currentTimeMillis()))));
    boolean overwriteGenome = false;

    // Genome name, overwrite
    if (cl.hasOption('n') || cl.hasOption("name"))
      genomeName = cl.getOptionValue('n');
    if (cl.hasOption('o') || cl.hasOption("overwrite"))
      overwriteGenome = true;

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
    setupGenomeDirectory(genomeName, overwriteGenome);
    log.info("Reference genome build: " + genome.getBuildName());
    }

  public void applyMutations() throws IOException, ExecutionException, InterruptedException
    {
    log.info("Applying mutations to " + genome.getBuildName());
    if (applySM)
      applySmallMutations();
    if (applySV)
      applyStructuraVariations();
    }

  // Applies mutations first at the 1kb or less level then structural
  private void applySmallMutations() throws IOException, InterruptedException, ExecutionException
    {
    log.info("Apply fragment level mutations.");
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
    log.info("Small mutation step finished on " + tasks.size() + " chromosomes.");
    smallMutationExcecutor.shutdown();
    }

   // Applies the larger, or location-based variations (>1kb).
  private void applyStructuraVariations() throws IOException, InterruptedException, ExecutionException
    {
    log.info("Apply SVs");
    structuralVariationExecutor = Executors.newFixedThreadPool(threads);
    CompletionService taskPool = new ExecutorCompletionService<String>(structuralVariationExecutor);

    File tmpDir = new File(genomeProperties.getProperty("dir.tmp"), genome.getGenomeDirectory().getName());

    File svWriterPath = new File(genome.getGenomeDirectory(), "structural-variations");
    if (!svWriterPath.exists() || !svWriterPath.isDirectory()) svWriterPath.mkdir();
    genome.setMutationDirectory(svWriterPath);

    Chromosome chr17 = genome.getChromosome("17");
    Chromosome chr11 = genome.getChromosome("11");

    /**** DERIVATIVE - translocation of band 11q21 to 17, 17 will be the derivative ****/
    FASTAHeader header = new FASTAHeader("figg", "chr17", "Derivative, 11q21 insert at 24000000", genome.getBuildName());
    FASTAWriter writer = new FASTAWriter( new File(genome.getGenomeDirectory(), "chr17der.fa"), header );
//
//    chr17.getFASTAReader().reset();
//    chr17.getFASTAReader().streamToWriter(24000000, writer);
//
//    DNASequence seq_11q21 = chr11.readSequence(92800001, 97200000);
//    writer.write(seq_11q21.getSequence());
//
//    // read the rest of the file
//    int window = 1000;
//    DNASequence remainder;
//    while (true)
//      {
//      remainder = chr17.readSequence(window);
//      writer.write(remainder.getSequence());
//      if (remainder.getLength() < window) break;
//      }
//    writer.close();


    /*** Delete 7q22-7q32 ***/
    // q22 98000001   q32 132600000
    header = new FASTAHeader("figg", "chr7", "Delete q22-q32", genome.getBuildName());
    writer = new FASTAWriter( new File(genome.getGenomeDirectory(), "chr7.fa"), header );

    Chromosome chr7 = genome.getChromosome("7");
    chr7.getFASTAReader().streamToWriter(98000001, writer);

    // start at the end of the band and output to the end of the chromosome
    int start = 132600000;
    int chars = 5000;
    writer.write(chr7.getFASTAReader().readSequenceFromLocation(start, chars));
    while(true)
      {
      String seq = chr7.getFASTAReader().readSequence(chars);
      writer.write(seq);
      if (seq.length() < chars) break;
      }
    writer.close();

    structuralVariationExecutor.shutdown();
    }

  // Create the directory where all fasta and mutation files will write
  private void setupGenomeDirectory(String name, boolean overwrite) throws IOException
    {
    File genomeDirectory = new File(genomeProperties.getProperty("dir.insilico"), name);

    if (genomeDirectory.exists() && overwrite)
      {
      log.warn("Overwriting " + genomeDirectory.getAbsolutePath());
      org.apache.commons.io.FileUtils.deleteDirectory(genomeDirectory);
      }
    else if (genomeDirectory.exists() && !overwrite)
      throw new IOException(genomeDirectory + " exists, cannot overwrite");

    genome.setGenomeDirectory(genomeDirectory);
    genome.setBuildName(name);

    File mutWriterPath = new File(genomeDirectory, "mutations");
    if (!mutWriterPath.exists() || !mutWriterPath.isDirectory()) mutWriterPath.mkdir();

    genome.setMutationDirectory(mutWriterPath);
    }

  // Adds chromosomes to the genome.  Either from the command line, or from the assembly directory
  private void setupChromosomes(List<String> chromosomes) throws IOException, ProbabilityException, InstantiationException, IllegalAccessException
    {
    // Set up the chromosomes in the genome that will be mutated.
    File fastaDir = new File(genomeProperties.getProperty("dir.assembly"));
    if (chromosomes.size() > 0)
      {
      for (String c : chromosomes)
        {
        Chromosome chromosome = new Chromosome(c, FileUtils.getFASTA(c, fastaDir));
        chromosome.setVariantList(variantUtils.getVariantList(chromosome.getName()));
        genome.addChromosome(chromosome);
        }
      }
    else
      genome.addChromosomes(FileUtils.getChromosomesFromFASTA(fastaDir));
    log.info("Reference genome has: " + genome.getChromosomes().length + " chromosomes");
    }

  // Variable initialization. Most of it is done in the Spring configuration files.
  private void init()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});

    // be nice to autowire this so I don't have to make calls into Spring but not important for now
    genome = (Genome) context.getBean("genome");
    variantUtils = (VariantUtils) context.getBean("variantUtils");
    genomeProperties = (Properties) context.getBean("genomeProperties");

    if (!genomeProperties.containsKey("dir.insilico") ||
        !genomeProperties.containsKey("dir.assembly") ||
        !genomeProperties.containsKey("dir.tmp"))
      {
      log.error("One of the directory definitions (dir.insilico, dir.assembly, dir.tmp) are missing from the properties file. Aborting.");
      System.exit(-1);
      }

    }

  // exactly what it sounds like...
  private CommandLine parseCommandLine(String[] args)
    {
    Options options = new Options();
    options.addOption("n", "name", true, "Genome directory name, if not provided a random name is generated.");
    options.addOption("o", "overwrite", false, "Overwrite genome directory if name already exists. [false]");
    options.addOption("t", "threads", true, "Number of concurrent threads. Each thread handles a single chromosome. [5]");
    options.addOption("c", "chromosome", true, "List of chromosomes to use/mutate, comma-separated (e.g.  21,3," +
        "X). If not included chromosomes will be determined by \n" + "the fasta files found in the" +
        " dir.assembly directory (see genome.properties).");
    options.addOption("s", "structural-variation", false, "Apply structural variations to genome. [false]");
    options.addOption("f", "fragment-variation", false, "Apply small scale (fragment) variation to genome. [false]");

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
      if (!cl.hasOption('s') && !cl.hasOption('f'))
        {
        System.out.println("One of the following (or both) options is required: -s or -f");
        help.printHelp("<jar file>", options);
        System.exit(0);
        }
      }
    catch (ParseException e)
      {
      log.error(e);
      }

    log.info("Running InsilicoGenome with the following parameters: ");
    for (Option opt : cl.getOptions())
      log.info("\t-" + opt.getLongOpt() + " '" + opt.getValue("true") + "'");

    if (cl.hasOption('s'))
      applySV = true;
    if (cl.hasOption('f'))
      applySM = true;

    return cl;
    }

  }
