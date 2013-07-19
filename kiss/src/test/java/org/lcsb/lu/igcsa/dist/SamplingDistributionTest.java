/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.dist;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class) @ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class SamplingDistributionTest
  {
  static Logger log = Logger.getLogger(SamplingDistributionTest.class.getName());

  // these are actually real probabilities from chromosome instability analysis
  private double[] probs = new double[]{0.665661950251339, 0.245478909346843, 0.49551345077658, 0.192048633134835, 0.247391069987363, 0.304565088577821, 0.3644265785891, 0.521448092663657, 0.899817977360762, 0.191975410414122, 0.66652775971321, 0.438477371785935, 0.2584635372864, 0.89730984587992, 0.246157580434442, 0.28260145778382, 0.533690455711098, 0.55321382427192, 0.495474089189846, 0.183010246939013, 0.406194615853718, 0.999785636005812};


  private SamplingDistribution sd;

  private HashMap<String, Integer> count = new HashMap<String, Integer>();

  @Before
  public void setUp() throws Exception
    {
    for (int i=0;i<probs.length;i++)
      count.put( String.valueOf(i+1), 0 );

    sd = new SamplingDistribution(probs, 4);
    assertNotNull(sd);
    }

  @Test
  public void testRoundedSamples() throws Exception
    {
    sd = new SamplingDistribution(probs, 2);
    for (int i=0; i<=10000; i++)
      {
      String chr = (String) sd.sample();
      count.put(chr, count.get(chr) + 1);
      }

    for (String c: count.keySet())
      assertTrue(count.get(c) > 0);
    }


  @Test
  public void testSample() throws Exception
    {
    assertNotNull(sd.sample());


    for (int i=0; i<=10000; i++)
      {
      String chr = (String) sd.sample();
      count.put(chr, count.get(chr) + 1);
      }

    for (String c: count.keySet())
      assertTrue(count.get(c) > 0);
    }


  }
