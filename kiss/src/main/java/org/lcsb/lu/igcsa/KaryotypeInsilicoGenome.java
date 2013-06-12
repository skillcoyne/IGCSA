package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.Translocation;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.*;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.utils.KaryotypePropertiesUtil;
import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class KaryotypeInsilicoGenome
  {
  static Logger log = Logger.getLogger(KaryotypeInsilicoGenome.class.getName());

  // Spring
  private ApplicationContext context;

  protected Properties genomeProperties;
  private Properties karyotypeProperties;
  protected Karyotype genome;


  public KaryotypeInsilicoGenome(ApplicationContext context, CommandLine cl) throws Exception
    {
    this.context = context;
    init(cl.getOptionValue('n'));

    List<String> chromosomes = new ArrayList<String>();
    if (cl.hasOption('c') || cl.hasOption("chromosome"))
      { // get specific chromosome from command line if required
      for (String c : cl.getOptionValue('c').split(","))
        chromosomes.add(c);
      }
    setupChromosomes(chromosomes);
    }

  public void applyMutations()
    {

    }



  // Applies the larger, or location-based variations (>1kb).
  private void applyStructuraVariations() throws IOException, InterruptedException, ExecutionException
    {
    log.info("Apply SVs");
    //    structuralVariationExecutor = Executors.newFixedThreadPool(threads);
    //    CompletionService taskPool = new ExecutorCompletionService<String>(structuralVariationExecutor);

    File tmpDir = new File(genomeProperties.getProperty("dir.tmp"), genome.getGenomeDirectory().getName());

    File svWriterPath = new File(genome.getGenomeDirectory(), "structural-variations");
    if (!svWriterPath.exists() || !svWriterPath.isDirectory()) svWriterPath.mkdir();
    genome.setMutationDirectory(svWriterPath);

    Chromosome from = genome.getChromosome("11");
    Chromosome to = genome.getChromosome("14");

    /**** DERIVATIVE - translocation of band 11q21 to 17, 17 will be the derivative ****/
    FASTAHeader header = new FASTAHeader("figg", "chr14", "11q13->14q32", genome.getBuildName());
    FASTAWriter writer = new FASTAWriter( new File(genome.getGenomeDirectory(), "chr14der.fa"), header );

    //44,X,-Y,-6,t(11;14)(q13;q32),add(22)(q13),del(7)(q22)
    //    Karyotype karyotype = new Karyotype(44, "XY", genome);
    //    karyotype.loseAneuploidy(genome.getChromosome("Y"));
    //    karyotype.loseAneuploidy(genome.getChromosome("6"));

    genome.loseChromosome("Y");
    genome.loseChromosome("6");

    /*
    TODO Karyotype is looking like it should be an implementation of Genome with aberrations tied to it.  There's too much calling of chromosomes going on here
     */

    // Band names don't actually matter, just locations
    //    Translocation translocation = new Translocation(from, to);
    //
    //    translocation.addFragment( new ChromosomeFragment(from, "q13", new Location(63400001, 77100000)) );
    //    translocation.addFragment( new ChromosomeFragment(to, "q32", new Location(89800001, 107349540)) );
    //
    //    Deletion deletion = new Deletion(genome.getChromosome("7"));
    //    deletion.addFragment( new ChromosomeFragment( genome.getChromosome("7"), "q22", new Location(98000001, 159138663) ) );
    //
    //    Addition addition = new Addition(genome.getChromosome("22"));
    //    addition.addFragment( new ChromosomeFragment( genome.getChromosome("22"), "q13", new Location(37600001, 51304566)));


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

    //structuralVariationExecutor.shutdown();
    }



  // Adds chromosomes to the genome.  Either from the command line, or from the assembly directory
  private void setupChromosomes(List<String> chromosomes) throws IOException, ProbabilityException, InstantiationException, IllegalAccessException
    {
    // Set up the chromosomes in the genome that will be mutated.
    File fastaDir = new File(genomeProperties.getProperty("dir.assembly"));
    /*
    If there are already chromosomes set up in the named genome directory we use those because the means fragment variations were applied first.
    If not we'll start from the assembly.
     */
    if (genome.getGenomeDirectory().listFiles().length > 0)
      fastaDir = genome.getGenomeDirectory();

    log.info("Genome directory to read from is: " + fastaDir.getAbsolutePath());

    if (chromosomes.size() > 0)
      {
      for (String c : chromosomes)
        {
        Chromosome chromosome = new Chromosome(c, FileUtils.getFASTA(c, fastaDir));
        genome.addChromosome(chromosome);
        }
      }
    else
      genome.addChromosomes(FileUtils.getChromosomesFromFASTA(fastaDir));

    // TODO set up aberrations, which are a bit different from variations as done in the fragment generator
    handleAneuploidy();
    genome.setAberrations(karyotypeProperties);


    log.info("Karyotype genome has: " + genome.getChromosomes().length + " chromosomes");
    }

  // TODO this will change to when I figure out how I'm pull the information from the database
  private void handleAneuploidy()
    {
    for (String chr :karyotypeProperties.getProperty("gain").split(","))
      genome.addChromosome(new Chromosome(chr));
    karyotypeProperties.remove("gain");

    for (String chr :karyotypeProperties.getProperty("loss").split(","))
      genome.loseChromosome(chr);
    karyotypeProperties.remove("loss");
    }

  // get all beans
  private void init(String name) throws Exception
    {
    genomeProperties = (Properties) context.getBean("genomeProperties");
    karyotypeProperties = (Properties) context.getBean("karyotypeProperties");

    genome = (Karyotype) context.getBean("karyotype");
    genome.setKaryotypeDefinition(Integer.valueOf(karyotypeProperties.getProperty("ploidy")), karyotypeProperties.getProperty("sex"));

    // don't need them mucking up the aberrations
    karyotypeProperties.remove("ploidy");
    karyotypeProperties.remove("sex");

    // should be reading previously mutated genome
    File genomeDirectory = new File(genomeProperties.getProperty("dir.insilico"), name);

    if (!genomeDirectory.exists())
      throw new IllegalArgumentException(genomeDirectory.getAbsolutePath() + " was not created.");

    genome.setGenomeDirectory(genomeDirectory);
    }




  }
