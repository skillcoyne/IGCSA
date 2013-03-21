package org.lcsb.lu.igcsa.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
public class GCBinDAOTest
  {

  @Autowired
  private GCBinDAO binDAO;


  @Test
  public void testGetBin() throws Exception
    {
    Bin b = binDAO.getBinById("5", 6);
    assertEquals(b.getBinId(), 6);
    assertEquals(b.getMin(), 415);
    assertEquals(b.getMax(), 498);
    assertEquals(b.getSize(), 46840);
    }


  @Test
  public void testGetBinByGC() throws Exception
    {
    Bin b = binDAO.getBinByGC("5", 311);
    assertEquals(b.getBinId(), 4);
    assertEquals(b.getMin(), 249);
    assertEquals(b.getMax(), 332);
    assertEquals(b.getSize(), 27733);
    }


  @Test
  public void testGetBins() throws Exception
    {
    Bin[] bins = binDAO.getBins("17");
    assertEquals(bins.length, 10);
    assertEquals(bins[0].getMin(), 0);
    assertEquals(bins[bins.length-1].getMax(), 860);
    }
  }
