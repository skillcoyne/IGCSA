package org.lcsb.lu.igcsa.genome;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SequenceTest
  {
  private String Nucleotides = "actcgccactttgc";
  private Sequence Sequence;

  @Before
  public void setUp() throws Exception
    {
    Sequence = new Sequence(Nucleotides);
    assertNotNull(Sequence);
    }

  @Test
  public void testAddNucleotides() throws Exception
    {
    try
      { Sequence.addNucleotides("swacp"); }
    catch (Exception e)
      { assertNotNull(e); }

    Sequence.addNucleotides("aatg");
    assertEquals(Sequence.getLength(), Nucleotides.length()+4);
    }

  @Test
  public void testGetSequence() throws Exception
    {
    assertEquals(Sequence.getSequence(), Nucleotides);
    }
  }
