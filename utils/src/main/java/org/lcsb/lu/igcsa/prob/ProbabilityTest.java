package org.lcsb.lu.igcsa.prob;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
@RunWith (SpringJUnit4ClassRunner.class) @ContextConfiguration (locations = {"classpath:test-spring-config.xml"})
public class ProbabilityTest
  {
  private Probability probability;

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

    this.probability = new Probability(nucleotides);
    assertNotNull(probability);

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 100; i++)
      buf.append((String) probability.roll());

    // I think these should always hold true...but...
    assertTrue(StringUtils.countOccurrencesOf(buf.toString(), "C") >= 4);
    assertTrue(StringUtils.countOccurrencesOf(buf.toString(), "G") >= 3);
    assertTrue(StringUtils.countOccurrencesOf(buf.toString(), "A") >= 2);
    assertTrue(StringUtils.countOccurrencesOf(buf.toString(), "T") >= 1);
    }

  @Test
  public void testRandomWithZero() throws Exception
    {
    nucleotides.put("A", 0.0);
    nucleotides.put("C", 0.5);
    nucleotides.put("T", 0.2);
    nucleotides.put("G", 0.3);

    this.probability = new Probability(nucleotides);
    assertNotNull(probability);

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 10; i++)
      buf.append((String) probability.roll());

    // Something with prob of 0 should never occur
    assertTrue(StringUtils.countOccurrencesOf(buf.toString(), "A") == 0);
    }

  @Test
  public void testRandomWithEqualProb() throws Exception
    {
    nucleotides.put("A", 0.0);
    nucleotides.put("C", 0.0);
    nucleotides.put("T", 0.5);
    nucleotides.put("G", 0.5);

    this.probability = new Probability(nucleotides);
    assertNotNull(probability);

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 100; i++)
      buf.append((String) probability.roll());

    assertTrue(StringUtils.countOccurrencesOf(buf.toString(), "T") >= 30);
    assertTrue(StringUtils.countOccurrencesOf(buf.toString(), "G") >= 30);
    }

  @Test
  public void testRounding() throws Exception
    {
    nucleotides.put("1", 0.0467956972956594);
    nucleotides.put("2", 0.0193356563896674);
    nucleotides.put("3", 0.0314559503067949);
    nucleotides.put("4", 0.0172430118930384);
    nucleotides.put("5", 0.0311718809181123);
    nucleotides.put("6", 0.0281607453980759);
    nucleotides.put("7", 0.0576092720248466);
    nucleotides.put("8", 0.0297231270358306);
    nucleotides.put("9", 0.0882224831452163);
    nucleotides.put("10", 0.0285489735626089);
    nucleotides.put("11", 0.0775320051511249);
    nucleotides.put("12", 0.092616089690175);
    nucleotides.put("13", 0.0200079539428831);
    nucleotides.put("14", 0.0422127111582456);
    nucleotides.put("15", 0.0353287629725021);
    nucleotides.put("16", 0.0331414286796455);
    nucleotides.put("17", 0.0769165214756458);
    nucleotides.put("18", 0.0226403302780092);
    nucleotides.put("19", 0.0252632376335126);
    nucleotides.put("20", 0.0508484205741989);
    nucleotides.put("21", 0.0944814786758579);
    nucleotides.put("22", 0.0391636997197182);
    nucleotides.put("X", 0.0115805620786304);

    HashMap<String, Double> count = new HashMap<String, Double>();
    for (Object obj: nucleotides.keySet()) count.put((String) obj, 0.0);

    this.probability = new Probability(nucleotides);
    for (int i = 0; i < 1000; i++)
      {
      String s = (String) this.probability.roll();
      count.put(s, count.get(s) + 1);
      }
    assertEquals(count.size(), 23);

    int max = (int) new Max().evaluate( ArrayUtils.toPrimitive(count.values().toArray(new Double[count.size()])) );

    for (Map.Entry<String, Double> entry: count.entrySet())
      {
      if (entry.getValue().equals(max))
        assertEquals(entry.getKey(), "21");
      }
    }

  }
