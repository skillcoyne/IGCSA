package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.generator.KaryotypeGenerator;
import org.lcsb.lu.igcsa.genome.*;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

  @Autowired
  private KaryotypeGenerator karyotypeGenerator;

  private int count = 2;


  public KaryotypeInsilicoGenome(ApplicationContext context, CommandLine cl) throws Exception
    {
    this.context = context;
    init(cl.getOptionValue('n'));

    count = Integer.parseInt(cl.getOptionValue('s', "2"));
    }

  public void generateKaryotypes()
    {
    for (int i = 0; i < count; i++)
      {
      try
        {
        log.info("Apply SVs");
        //File tmpDir = new File(genomeProperties.getProperty("dir.tmp"), genome.getGenomeDirectory().getName());

        File svWriterPath = new File(genome.getGenomeDirectory(), "structural-variations");
        if (!svWriterPath.exists() || !svWriterPath.isDirectory()) svWriterPath.mkdir();

        Karyotype karyotype = genome.copy();
        karyotype.setBuildName(karyotype.getBuildName() + "-kt-" + i);

        karyotype.setMutationDirectory(svWriterPath);
        karyotype = karyotypeGenerator.generateKaryotypes(karyotype);

        MutationWriter ploidyWriter = new MutationWriter(new File(svWriterPath, "normal-ploidy.txt"), MutationWriter.PLOIDY);

        for (Chromosome c : karyotype.getChromosomes())
          {
          if (karyotype.ploidyCount(c.getName()) > 0)
            org.apache.commons.io.FileUtils.copyFile(c.getFASTA(), new File(karyotype.getGenomeDirectory(), c.getFASTA().getName()));

          ploidyWriter.write(new Mutation(c.getName(), karyotype.ploidyCount(c.getName())));
          }
        ploidyWriter.close();
        }
      catch (ProbabilityException e)
        {
        throw new RuntimeException(e);
        }
      catch (IOException e)
        {
        log.error(e);
        }
      }
    }


  /* This will expect a set up that includes all chromosomes defined in the probability database. This means providing
     all of the fasta files for a genome in the fragment stage as well... */
  private void initializeKaryotype(Karyotype kt, List<String> chromosomes) throws IOException, ProbabilityException,
                                                                                  IllegalAccessException, InstantiationException
    {
    File genomeDirectory = new File(genomeProperties.getProperty("dir.insilico"), kt.getBuildName());

    if (!genomeDirectory.exists()) throw new IllegalArgumentException(genomeDirectory.getAbsolutePath() + " was not created.");

    genome.setGenomeDirectory(genomeDirectory);

    // Set up the chromosomes in the genome that will be mutated, these come from the previously mutated genome if there was one
    File fastaDir = new File(genomeProperties.getProperty("dir.assembly"));
    /*
   If there are already chromosomes set up in the named genome directory we use those because the means fragment variations were
   applied first. If not we'll start from the assembly.
    */
    if (genome.getGenomeDirectory().listFiles().length > 0) fastaDir = genome.getGenomeDirectory();

    log.info("Genome directory to read from is: " + fastaDir.getAbsolutePath());
    genome.addChromosomes(FileUtils.getChromosomesFromFASTA(fastaDir));
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

      for (Chromosome c : genome.getChromosomes())
        {
        if (genome.ploidyCount(c.getName()) > 0)
          org.apache.commons.io.FileUtils.copyFile(c.getFASTA(), new File(genome.getGenomeDirectory(), c.getFASTA().getName()));

        ploidyWriter.write(new Mutation(c.getName(), genome.ploidyCount(c.getName())));
        }
      ploidyWriter.close();
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }

    }


  // get all beans
  private void init(String name) throws Exception
    {
    genomeProperties = (Properties) context.getBean("genomeProperties");
    karyotypeProperties = (Properties) context.getBean("karyotypeProperties");

    genome = (Karyotype) context.getBean("karyotype");
    genome.setKaryotypeDefinition(Integer.valueOf(karyotypeProperties.getProperty("ploidy")), karyotypeProperties.getProperty("sex"));

    // should be reading previously mutated genome
    genome.setBuildName(name + " karyotype");
    }

  }
