package org.lcsb.lu.igcsa.genome;

import org.apache.commons.codec.binary.BinaryCodec;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class DNASequenceTest
  {
  private String nucleotides = "actcgccactttgc";
  private DNASequence dnaSequence;

  @Before
  public void setUp() throws Exception
    {
    dnaSequence = new DNASequence(nucleotides);
    assertNotNull(dnaSequence);
    }

  @Test
  public void testGetLength() throws Exception
    {
    assertEquals(dnaSequence.getLength(), nucleotides.length());
    }

  @Test
  public void testAddNucleotides() throws Exception
    {
    try
      { dnaSequence.addNucleotides("swcp"); }
    catch (Exception e)
      { assertNotNull(e); }

    assertEquals(dnaSequence.getLength(), nucleotides.length());

    dnaSequence.addNucleotides("aatg");
    assertEquals(dnaSequence.getLength(), nucleotides.length() + 4);
    }

  @Test
  public void testGetSequence() throws Exception
    {
    assertEquals(dnaSequence.getSequence(), nucleotides.toUpperCase());
    }


  }
