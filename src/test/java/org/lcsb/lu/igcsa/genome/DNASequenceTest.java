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
public class DNASequenceTest
  {
  private String Nucleotides = "actcgccactttgc";
  private DNASequence DNASequence;

  @Before
  public void setUp() throws Exception
    {
    DNASequence = new DNASequence(Nucleotides);
    assertNotNull(DNASequence);
    }

  @Test
  public void testAddNucleotides() throws Exception
    {
    try
      { DNASequence.addNucleotides("swacp"); }
    catch (Exception e)
      { assertNotNull(e); }

    DNASequence.addNucleotides("aatg");
    assertEquals(DNASequence.getLength(), Nucleotides.length()+4);
    }

  @Test
  public void testGetSequence() throws Exception
    {
    assertEquals(DNASequence.getSequence(), Nucleotides);
    }
  }
