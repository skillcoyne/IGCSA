package org.lcsb.lu.igcsa.testGenome;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Genome;
import org.springframework.beans.factory.annotation.Autowired;
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
  private Chromosome[] chromosomes;
  private File fastaFile;

  @Autowired
  private Genome testGenome;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    fastaFile = new File(testUrl.toURI());

    chromosomes = new Chromosome[10];
    for (int i = 1; i <= 10; i++) // just don't want a chromosome named '0'
      {
      chromosomes[i - 1] = new Chromosome(Integer.toString(i));
      }

    assertNotNull("Genome object failed to create", testGenome);
    }

  @Test
  public void testGetChromosomes() throws Exception
    {
    testGenome.addChromosomes(chromosomes);
    assertEquals("Chromosomes names 1-10 added", testGenome.getChromosomes().length, 10);
    for (Chromosome c : chromosomes)
      { assertTrue("Chromosome " + c.getName() + " should be in the testGenome", testGenome.hasChromosome(c.getName())); }
    }

  @Test
  public void testMutateChromosome() throws Exception
    {
    assertNotNull(testGenome.getVariantTypes());
    assertEquals(testGenome.getVariantTypes().size(), 5);

    testGenome.addChromosome(new Chromosome("19", fastaFile));

    Chromosome newChr;
    for(Chromosome chr: testGenome.getChromosomes())
      {
      System.out.println(chr.getName());
      System.out.println(chr.getFASTA().getAbsolutePath());
      newChr = testGenome.mutate(chr, 20);
      }

//    testGenome = GenomeUtils.setupSizeVariation(properties.getVariationProperty("del"), testGenome, new Deletion());
//    testGenome = GenomeUtils.setupSNPs(properties.getVariationProperty("snp"), testGenome);
//    assertEquals(testGenome.getVariations().size(),5);
//
//    testGenome.addChromosome(new Chromosome("1", fastaFile));
//    testGenome.addChromosome(new Chromosome("2", fastaFile));
//    assertTrue(testGenome.hasChromosome("1"));
//    assertTrue(testGenome.hasChromosome("2"));
//
//    //testGenome.mutate();
//
//
//    int window = 50;
//    String seq;
//    Map<Variation, ProbabilityList> variations = testGenome.getVariations();
//    for (Chromosome chr : testGenome.getChromosomes())
//      {
//      int mutatedSequences = 0;
//      while (true)
//        {
//        seq = chr.readSequence(window).getSequence();
//        for (Iterator<Variation> it = variations.keySet().iterator(); it.hasNext();)
//          {
//          Variation var = it.next();
//          if (!var.getClass().isInstance(new SNP())) continue;
//          var.setProbabilityList( variations.get(var) );
//          DNASequence newSequence = var.mutateSequence( new DNASequence(seq) );
//          if (!newSequence.toString().equals(seq)) ++mutatedSequences;
//          }
//        if (seq.length() < window) break;
//        }
//      assertTrue("At least one sequence should have mutated", mutatedSequences > 0);
//      }
    }


  }
