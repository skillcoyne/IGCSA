package org.lcsb.lu.igcsa.genome;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.utils.GenomeProperties;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenomeTest
  {
  private ReferenceGenome genome;
  private Chromosome[] chromosomes;

  @Before
  public void setUp() throws Exception
    {
//    GenomeProperties props = GenomeProperties.readPropertiesFile("test.properties", GenomeProperties.GenomeType.NORMAL);
//    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
//    FileUtils.getChromosomeFromFASTA(new File(testUrl));
    chromosomes = new Chromosome[10];
    for (int i = 1; i<=10; i++) // just don't want a chromosome named '0'
      {
      chromosomes[i-1] =  new Chromosome( Integer.toString(i), "actcgcttac"  );
      }
    genome = new ReferenceGenome("testBuild");
    assertNotNull("Genome object failed to create", genome);
    }

  @Test
  public void testAddChromosome() throws Exception
    {
    genome.addChromosome( new Chromosome("George", "actcgcgt") );
    assertTrue("Should have a chromosome named 'George'", genome.hasChromosome("George"));
    }

  @Test
  public void testGetChromosomes() throws Exception
    {
    genome.addChromosomes(chromosomes);
    assertEquals("Chromosomes names 1-10 added", genome.getChromosomes().length, 10);
    for (Chromosome c: chromosomes)
      { assertTrue("Chromosome " + c.getName() + " should be in the genome", genome.hasChromosome(c.getName())); }
    }

  @Test
  public void testMutateChromosome() throws Exception
    {

    }

  }
