package org.lcsb.lu.igcsa.database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentVariationDAO;
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
public class FragmentVariationDAOTest
  {
  @Autowired
  private FragmentVariationDAO fragmentDAO;

  @Test
  public void testGetFragment() throws Exception
    {
    Fragment frag = fragmentDAO.getFragment("5", 1, 63);
    assertNull(frag);

    frag = fragmentDAO.getFragment("5", 6, 63);
    assertNotNull(frag);

    assertEquals(frag.getSNV(), 27);
    assertEquals(frag.getDeletion(), 2);
    assertEquals(frag.getInsertion(), 4);
    assertEquals(frag.getSubstitution(), 0);
    assertEquals(frag.getSeqAlt(), 0);
    assertEquals(frag.getTandemRepeat(), 0);
    }
  }
