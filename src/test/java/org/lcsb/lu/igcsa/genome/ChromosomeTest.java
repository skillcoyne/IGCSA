package org.lcsb.lu.igcsa.genome;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
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
    assertEquals(chr.getDNASequence(), sequence);
    }

  public void testGetName() throws Exception
    {
    assertEquals(name, chr.getName());
    }
  }
