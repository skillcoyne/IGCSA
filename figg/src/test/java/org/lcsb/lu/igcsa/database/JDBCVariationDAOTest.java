package org.lcsb.lu.igcsa.database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.VariationDAO;
import org.lcsb.lu.igcsa.database.normal.Variations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class JDBCVariationDAOTest
  {

  @Autowired
  private VariationDAO testVariationDAO;

  @Test
  public void testGetVariations() throws Exception
    {
    Variations vars = testVariationDAO.getVariations();
    assertEquals(vars.getVariationNametoID().size(), 6);
    }
  }
