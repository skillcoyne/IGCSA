/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.dist;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class ContinuousDistributionTest
  {
  static Logger log = Logger.getLogger(ContinuousDistributionTest.class.getName());

  @Test
  public void testSampleNormal() throws Exception
    {
    ContinuousDistribution cd = new ContinuousDistribution(2, 3);
    Map<Double, Integer> counts = new HashMap<Double, Integer>();
    for (int i=0; i<100; i++)
      {
      double s = cd.sample();
      assertTrue( s >= 0 );

      if (!counts.containsKey(s)) counts.put(s, 0);
      counts.put(s, counts.get(s) + 1);
      }
    log.info(counts);

    assertTrue(counts.keySet().size() > 3);
    }



  @Test
  public void testSamplePoisson() throws Exception
    {
    ContinuousDistribution cd = new ContinuousDistribution(3);

    Map<Double, Integer> counts = new HashMap<Double, Integer>();
    for (int i=0; i<100; i++)
      {
      double s = cd.sample();
      assertTrue( s >= 0 );

      if (!counts.containsKey(s)) counts.put(s, 0);
      counts.put(s, counts.get(s) + 1);
      }
    assertTrue(counts.keySet().size() > 3);
    }
  }
