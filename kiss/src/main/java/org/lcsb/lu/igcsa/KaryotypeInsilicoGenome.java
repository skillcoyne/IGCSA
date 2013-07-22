package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.Aberration;
import org.lcsb.lu.igcsa.aberrations.Translocation;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
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
    log.info("Apply SVs");
    //File tmpDir = new File(genomeProperties.getProperty("dir.tmp"), genome.getGenomeDirectory().getName());

    File svWriterPath = new File(genome.getGenomeDirectory(), "structural-variations");
    if (!svWriterPath.exists() || !svWriterPath.isDirectory()) svWriterPath.mkdir();

    genome.setMutationDirectory(svWriterPath);

    try
      {
      genome.applyAberrations();

      MutationWriter ploidyWriter = new MutationWriter(new File(svWriterPath, "normal-ploidy.txt"), MutationWriter.PLOIDY);

      for (Chromosome c: genome.getChromosomes())
        {
        if (genome.ploidyCount(c.getName()) > 0)
          org.apache.commons.io.FileUtils.copyFile( c.getFASTA(), new File( genome.getGenomeDirectory(), c.getFASTA().getName()));

        ploidyWriter.write(new Mutation(c.getName(), genome.ploidyCount(c.getName())));
        }
      ploidyWriter.close();
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }

    }

  // Adds chromosomes to the genome.  Either from the command line, or from the assembly directory
  private void setupChromosomes(List<String> chromosomes) throws Exception, ProbabilityException, InstantiationException, IllegalAccessException, ClassNotFoundException
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
    //handleAneuploidy();
    genome.setAberrations(karyotypeProperties);

    log.info("Karyotype genome has: " + genome.getChromosomes().length + " chromosomes and " + genome.getDerivativeChromosomes().length +
     " derivative chromosomes");
    }

  // TODO this will change to when I figure out how I'm pull the information from the database
  private void handleAneuploidy()
    {
//    for (String chr :karyotypeProperties.getProperty("gain").split(","))
//      genome.chromosomeGain(chr);
//    karyotypeProperties.remove("gain");
//
//    for (String chr :karyotypeProperties.getProperty("loss").split(","))
//      genome.chromosomeLoss(chr);
//    karyotypeProperties.remove("loss");
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
    genome.setBuildName(name + " karyotype");
    }

  }
