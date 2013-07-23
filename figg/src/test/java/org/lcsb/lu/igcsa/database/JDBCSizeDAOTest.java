package org.lcsb.lu.igcsa.database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.SizeDAO;

import org.lcsb.lu.igcsa.prob.Probability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class JDBCSizeDAOTest
  {

  @Autowired
  private SizeDAO testSizeDAO;

  @Test
  public void testGetByVariation() throws Exception
    {
    Probability probability = testSizeDAO.getByVariation("deletion");
    assertNotNull(probability);
    assertEquals(probability.getProbabilities().size(), 2);
    }

  @Test
  public void getAll() throws Exception
    {
    Map<String, Probability> freq = testSizeDAO.getAll();
    assertEquals(freq.size(), 5);
    assertNotNull(freq.get("insertion"));
    }
  }
