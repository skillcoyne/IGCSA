package org.lcsb.lu.igcsa.population.utils;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.population.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class HammingDistanceTest
  {
  static Logger log = Logger.getLogger(HammingDistanceTest.class.getName());

  @Test
  public void testGetDistance() throws Exception
    {
    assertEquals(HammingDistance.getDistance("ACTG", "ACTG"), 0);
    assertEquals(HammingDistance.getDistance("ACTG", "ACTG"), HammingDistance.getDistance("ACTG".getBytes(), "ACTG".getBytes()));

    assertEquals(HammingDistance.getDistance("AATG", "ACTG"), 1);
    assertEquals(HammingDistance.getDistance("AATG", "ACTG"), HammingDistance.getDistance(new DNASequence("AATG"), new DNASequence("ACTG")));

    assertEquals(HammingDistance.getDistance("AATG", "ACGG"), 2);
    }

  }
