package org.lcsb.lu.igcsa.genome;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * org.lcsb.lu.igcsa.genome
 * User: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open source License Apache 2
 */
public class ChromosomeTest extends TestCase
  {
  private Chromosome chr;
  private String sequence = "actgccatg";
  private String name = "1";

  @Before
  public void setUp() throws Exception
    {
    chr = new Chromosome(name, sequence);
    }

  @After
  public void tearDown() throws Exception
    {}

  @Test
  public void testGetSequence() throws Exception
    {
    assertEquals(chr.getSequence(), sequence);
    }

  public void testGetLength() throws Exception
    {
    assertEquals(sequence.length(), chr.getLength());
    }

  public void testGetName() throws Exception
    {
    assertEquals(name, chr.getName());
    }
  }
