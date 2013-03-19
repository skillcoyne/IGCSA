package org.lcsb.lu.igcsa.prob;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

  private TreeMap<Object, Double> nucleotides;

  @Before
  public void setup()
    {
    nucleotides = new TreeMap<Object, Double>();
    }


  @Test
  public void testRandom() throws Exception
    {
    nucleotides.put("A", 0.2);
    nucleotides.put("C", 0.4);
    nucleotides.put("T", 0.1);
    nucleotides.put("G", 0.3);

    this.frequency = new Frequency( nucleotides );
    assertNotNull(frequency);

    StringBuffer buf = new StringBuffer();
    for (int i=0; i<10; i++) buf.append( (String) frequency.roll() );

    // I think these should always hold true...but...
    assertTrue( StringUtils.countOccurrencesOf(buf.toString(), "C") >= 3);
    assertTrue( StringUtils.countOccurrencesOf(buf.toString(), "G") >= 2);
    assertTrue( StringUtils.countOccurrencesOf(buf.toString(), "T") <= 1);

    }

  @Test
  public void testRandomWithZero() throws Exception
    {
    nucleotides.put("A", 0.0);
    nucleotides.put("C", 0.5);
    nucleotides.put("T", 0.2);
    nucleotides.put("G", 0.3);

    this.frequency = new Frequency( nucleotides );
    assertNotNull(frequency);

    StringBuffer buf = new StringBuffer();
    for (int i=0; i<10; i++) buf.append( (String) frequency.roll() );

    // Something with prob of 0 should never occur
    assertTrue( StringUtils.countOccurrencesOf(buf.toString(), "A") == 0);
    }

  @Test
  public void testRandomWithEqualProb() throws Exception
    {
    nucleotides.put("A", 0.0);
    nucleotides.put("C", 0.0);
    nucleotides.put("T", 0.5);
    nucleotides.put("G", 0.5);

    this.frequency = new Frequency( nucleotides );
    assertNotNull(frequency);

    StringBuffer buf = new StringBuffer();
    for (int i=0; i<100; i++) buf.append( (String) frequency.roll() );

    assertTrue( StringUtils.countOccurrencesOf(buf.toString(), "T") >= 30);
    assertTrue( StringUtils.countOccurrencesOf(buf.toString(), "G") >= 30);
    }


  }
