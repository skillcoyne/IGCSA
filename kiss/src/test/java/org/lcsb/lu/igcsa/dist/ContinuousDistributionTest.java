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

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class ContinuousDistributionTest
  {
  static Logger log = Logger.getLogger(ContinuousDistributionTest.class.getName());

  @Test
  public void testSampleNormal() throws Exception
    {
    ContinuousDistribution cd = new ContinuousDistribution(0, 1);
    }

  @Test
  public void testSamplePoisson() throws Exception
    {
    ContinuousDistribution cd = new ContinuousDistribution(3);

    int s = 100;
    double[] samples = new double[s];
    for (int i=0; i<s; i++)
      {
      samples[i] = cd.sample();
      log.info(samples[i]);
      }

    log.info(samples);

    }
  }
