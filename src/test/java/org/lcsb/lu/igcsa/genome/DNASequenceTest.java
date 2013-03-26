package org.lcsb.lu.igcsa.genome;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class DNASequenceTest
  {
  @Autowired
  private Properties testProperties;

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
    assertEquals(dnaSequence.getLength(), nucleotides.length()+4);
    }

  @Test
  public void testGetSequence() throws Exception
    {
    assertEquals(dnaSequence.getSequence(), nucleotides.toUpperCase());
    }
  }
