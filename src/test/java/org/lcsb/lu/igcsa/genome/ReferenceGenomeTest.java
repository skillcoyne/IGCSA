package org.lcsb.lu.igcsa.genome;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.utils.GenomeProperties;
import org.lcsb.lu.igcsa.utils.GenomeUtils;
import org.lcsb.lu.igcsa.variation.Deletion;
import org.lcsb.lu.igcsa.variation.SNP;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenomeTest
  {
  private Genome genome;
  private Chromosome[] chromosomes;
  private File fastaFile;
  private GenomeProperties properties;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    fastaFile = new File(testUrl.toURI());
    properties = GenomeProperties.readPropertiesFile("test.properties", GenomeProperties.GenomeType.NORMAL);


    chromosomes = new Chromosome[10];
    for (int i = 1; i <= 10; i++) // just don't want a chromosome named '0'
      {
      chromosomes[i - 1] = new Chromosome(Integer.toString(i), "actcgcttac");
      }
    genome = new ReferenceGenome("testBuild");
    assertNotNull("Genome object failed to create", genome);
    }

  @After
  public void tearDown() throws Exception
    {

    }

  @Test
  public void testAddChromosome() throws Exception
    {
    genome.addChromosome(new Chromosome("George", "actcgcgt"));
    assertTrue("Should have a chromosome named 'George'", genome.hasChromosome("George"));
    }

  @Test
  public void testGetChromosomes() throws Exception
    {
    genome.addChromosomes(chromosomes);
    assertEquals("Chromosomes names 1-10 added", genome.getChromosomes().length, 10);
    for (Chromosome c : chromosomes)
      { assertTrue("Chromosome " + c.getName() + " should be in the genome", genome.hasChromosome(c.getName())); }
    }

  @Test
  public void testMutateChromosome() throws Exception
    {
    genome = GenomeUtils.setupSizeVariation(properties.getVariationProperty("del"), genome, new Deletion());
    genome = GenomeUtils.setupSNPs(properties.getVariationProperty("snp"), genome);
    assertEquals(genome.getVariations().size(),5);

    genome.addChromosome(new Chromosome("1", fastaFile));
    genome.addChromosome(new Chromosome("2", fastaFile));
    assertTrue(genome.hasChromosome("1"));
    assertTrue(genome.hasChromosome("2"));


    int window = 50;
    String seq;
    Map<Variation, ProbabilityList> variations = genome.getVariations();
    for (Chromosome chr : genome.getChromosomes())
      {
      int mutatedSequences = 0;
      while (true)
        {
        seq = chr.getDNASequence(window);
        for (Iterator<Variation> it = variations.keySet().iterator(); it.hasNext();)
          {
          Variation var = it.next();
          if (!var.getClass().isInstance(new SNP())) continue;
          var.setProbabilityList( variations.get(var) );
          DNASequence newSequence = var.mutateSequence( new DNASequence(seq) );
          if (!newSequence.toString().equals(seq)) ++mutatedSequences;
          }
        if (seq.length() < window) break;
        }
      assertTrue("At least one sequence should have mutated", mutatedSequences > 0);
      }
    }


  }
