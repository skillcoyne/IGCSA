package org.lcsb.lu.igcsa.prob;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FrequencyTest
  {
  private Frequency frequency;


  @Before
  public void setUp() throws Exception
    {
    Map<Object, Double> nucleotides = new HashMap<Object, Double>();
    nucleotides.put("A", 0.2);
    nucleotides.put("C", 0.4);
    nucleotides.put("T", 0.1);
    nucleotides.put("G", 0.3);

    this.frequency = new Frequency( nucleotides );
    assertNotNull(frequency);
    }

  @Test
  public void testRandom() throws Exception
    {
    String nuc = (String) frequency.random();
    System.out.println(nuc);
    }
  }
