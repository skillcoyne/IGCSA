package org.lcsb.lu.igcsa.genome;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.prob.Frequency;
import org.lcsb.lu.igcsa.variation.Variation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.*;

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
  private Properties testProperties;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    fastaFile = new File(testUrl.toURI());
    outputFasta = new File(testProperties.getProperty("dir.insilico") + "/test.fasta");

    Map<Object, Double> probs = new TreeMap<Object, Double>();
    probs.put(1, 0.9897);
    probs.put(5, 0.0086);
    probs.put(10, 0.0017);
    Frequency f = new Frequency(probs);

    // need to reload the genome each time or the add chromosome tests screw things up
    ApplicationContext context = new ClassPathXmlApplicationContext("test-spring-config.xml");
    testGenome = (Genome) context.getBean("testGenome");

    List<Variation> variationList = testGenome.getVariantTypes();
    for (Variation var: variationList)
      var.setSizeVariation(f);

    testGenome.setVariantTypes(variationList);

    assertNotNull("Genome object failed to create", testGenome);
    }

  @After
  public void tearDown() throws Exception
    {
    outputFasta.delete();
    testGenome = null;
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
    long origLength = outputFasta.length();

    assertNotNull(testGenome.getVariantTypes());
    assertEquals(testGenome.getVariantTypes().size(), 3);

    Chromosome origChr = new Chromosome("19", fastaFile);
    testGenome.addChromosome(origChr);

    assertEquals(testGenome.getChromosomes().length, 1);

    Mutable m = testGenome.mutate(testGenome.getChromosomes()[0], 50, writer);
    m.run();

    assertTrue(outputFasta.length() > origLength);

    Chromosome newChr = new Chromosome("19", outputFasta);
    assertNotSame(origChr.retrieveFullSequence(), newChr.retrieveFullSequence());
    }

  }
