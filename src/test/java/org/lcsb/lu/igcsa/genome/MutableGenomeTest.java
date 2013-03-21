package org.lcsb.lu.igcsa.testGenome;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.FragmentVariationDAO;
import org.lcsb.lu.igcsa.database.GCBinDAO;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.genome.MutableGenome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.testGenome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class MutableGenomeTest
  {
  private File fastaFile;
  private File outputFasta;

  private Genome testGenome;

  @Autowired
  private GCBinDAO gcBinDAO;

  @Autowired
  FragmentVariationDAO fragmentDAO;

  @Autowired
  private Properties testProperties;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    fastaFile = new File(testUrl.toURI());
    outputFasta = new File(testProperties.getProperty("dir.insilico") + "/test.fasta");

    ApplicationContext context = new ClassPathXmlApplicationContext("test-spring-config.xml");
    testGenome = (Genome) context.getBean("testReferenceGenome");

    assertNotNull("Genome object failed to create", testGenome);
    }

  @After
  public void tearDown() throws Exception
    {
    outputFasta.delete();
    }

  @Test
  public void testGetChromosomes() throws Exception
    {
    Chromosome[] chromosomes = new Chromosome[10];
    for (int i = 1; i <= 10; i++) chromosomes[i - 1] = new Chromosome(Integer.toString(i));

    testGenome.addChromosomes(chromosomes);
    assertEquals("Chromosomes names 1-10 added", testGenome.getChromosomes().length, 10);
    for (Chromosome c : chromosomes)
      { assertTrue("Chromosome " + c.getName() + " should be in the testGenome", testGenome.hasChromosome(c.getName())); }
    }

  @Test
  public void testMutateChromosome() throws Exception
    {
    FASTAHeader header = new FASTAHeader(">gi|12345|Test case for mutating genomes");
    FASTAWriter writer = new FASTAWriter(outputFasta, header);

    assertNotNull(testGenome.getVariantTypes());
    assertEquals(testGenome.getVariantTypes().size(), 3);

    Chromosome origChr = new Chromosome("19", fastaFile);
    testGenome.addChromosome(origChr);

    assertEquals(testGenome.getChromosomes().length, 1);

    Chromosome newChr = null;
    for(Chromosome chr: testGenome.getChromosomes())
      {
      assertEquals(chr.getName(), "19");
      newChr = testGenome.mutate(chr, 50, writer);
      }

    assertNotSame(origChr.retrieveFullSequence(), newChr.retrieveFullSequence());

    }


  }
