package org.lcsb.lu.igcsa;


import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.database.normal.FragmentVariationDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
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


/**
 * org.lcsb.lu.igcsa
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

//@ContextConfiguration (locations={"classpath:spring-config.xml"})
public class InsilicoGenome
  {
  static Logger log = Logger.getLogger(InsilicoGenome.class.getName());

//  protected FragmentVariationDAO fragmentDAO;
//  protected GCBinDAO binDAO;

  protected Properties normalGenomeProperties;

  // Defaults
  private int windowSize = 1000;

  protected Genome referenceGenome;


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

    long time = java.lang.System.currentTimeMillis();
    int individualId = new Random().nextInt( (int) (time*-1));

    setupReferenceGenome();
    createGenome( individualId );
    }


  public void createGenome(int id) throws IOException
    {
    File genomeDirectory = new File( normalGenomeProperties.getProperty("dir.insilico"), String.valueOf(id) );
    if (genomeDirectory.exists()) throw new IOException(genomeDirectory + " exists, cannot overwrite");
    else genomeDirectory.mkdirs();
    log.info(genomeDirectory.getAbsolutePath());

    referenceGenome.setMutationWriter( new MutationWriter(new File(genomeDirectory, "mutations.txt")));
    for (Chromosome chr : referenceGenome.getChromosomes()) //this could be done in threads, each chromosome can be mutated separately
      {
      log.info(chr.getName());
      try
        {
        FASTAHeader header = new FASTAHeader(">chromosome|" + chr.getName() + "|individual " + id);
        FASTAWriter writer = new FASTAWriter(new File(genomeDirectory, "chr" + chr.getName() + ".fa"), header);
        referenceGenome.mutate(chr, windowSize, writer);
        writer.close();
        }
      catch (IOException e)
        {
        e.printStackTrace();
        }
      }
    }


  /*
   * Sets up the reference genome based on the fasta files for the current build.
   */
  protected void setupReferenceGenome() throws FileNotFoundException, ProbabilityException, IllegalAccessException, InstantiationException
    {
    referenceGenome.addChromosomes(FileUtils.getChromosomesFromFASTA(new File(normalGenomeProperties.getProperty("dir.assembly"))));
    log.info("Reference genome build: " + referenceGenome.getBuildName());
    log.info("Reference genome has: " + referenceGenome.getChromosomes().length + " chromosomes");
    }


  /*
  Variable initialization. Most of it is done in the Spring configuration files.
   */
  private void init()
    {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");

//    fragmentDAO = (FragmentVariationDAO) context.getBean("FragmentDAO");
//    binDAO = (GCBinDAO) context.getBean("GCBinDAO");

    normalGenomeProperties = (Properties) context.getBean("normalGenomeProperties");

//    if (normalGenomeProperties.containsKey("window"))
//      this.windowSize = Integer.valueOf(normalGenomeProperties.getProperty("window"));

    referenceGenome = (Genome) context.getBean("referenceGenome");
    }

  }
