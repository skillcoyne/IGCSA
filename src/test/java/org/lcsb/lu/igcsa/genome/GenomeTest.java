package org.lcsb.lu.igcsa.genome;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * User: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open source License Apache 2
 */
public class GenomeTest
  {
  private Genome genome;
  private Collection<Chromosome> chromosomes;

  @Before
  public void setUp() throws Exception
    {
    chromosomes = new ArrayList<Chromosome>();
    for (int i = 1; i<=10; i++) { chromosomes.add( new Chromosome( Integer.toString(i), "actcgcttac" ) ); }
    genome = new Genome(chromosomes);
    }

  @After
  public void tearDown() throws Exception
    { }

  @Test
  public void testAddChromosome() throws Exception
    {
    genome.addChromosome( new Chromosome("X", "actcgcgt") );
    assertTrue(genome.hasChromosome("1"));
    }

  @Test
  public void testGetChromosomes() throws Exception
    {
    assertEquals(genome.getChromosomes().size(), 10);
    for (Chromosome c: chromosomes)
      { assertTrue(genome.hasChromosome(c.getName())); }
    }

  @Test
  public void testMutateChromosome() throws Exception
    {

    }

  }
