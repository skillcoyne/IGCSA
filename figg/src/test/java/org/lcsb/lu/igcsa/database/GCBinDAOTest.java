package org.lcsb.lu.igcsa.database;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.Range;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
  private GCBinDAO testGCBinDAO;


  @Test
  public void testGetBin() throws Exception
    {
    Bin b = testGCBinDAO.getBinById("5", 6);
    assertEquals(b.getBinId(), 6);
    assertEquals(b.getMin(), 415);
    assertEquals(b.getMax(), 498);
    assertEquals(b.getSize(), 46840);
    }


  @Test
  public void testGetBinByGC() throws Exception
    {
    Bin b = testGCBinDAO.getBinByGC("5", 311);
    assertEquals(b.getBinId(), 4);
    assertEquals(b.getMin(), 249);
    assertEquals(b.getMax(), 332);
    assertEquals(b.getSize(), 27733);
    }


  @Test
  public void testGetBins() throws Exception
    {
    Bin[] bins = testGCBinDAO.getBins("17");
    assertEquals(bins.length, 10);
    assertEquals(bins[0].getMin(), 0);
    assertEquals(bins[bins.length-1].getMax(), 860);
    }

  @Test
  public void testTime() throws Exception
    {
    int gc = 360;
    String chr = "9";

    long s = System.currentTimeMillis();
    Bin[] bins = testGCBinDAO.getBins(chr);
    long e = System.currentTimeMillis() - s;

    long s1 = System.currentTimeMillis();
    testGCBinDAO.getBinByGC(chr, gc);
    long e1 = System.currentTimeMillis() - s1;
    assertTrue(e1 <= e);

    long s2 = System.currentTimeMillis();
    Bin b = testGCBinDAO.getBinByGC(chr, gc);
    long e2 = System.currentTimeMillis() - s2;
    assertTrue(e2 <= e);
    assertTrue(e2 <= e1);

    //System.out.println("" + e + " " + e1 + " " + e2);


    assertEquals(b.getBinId(), 5);
    assertEquals(b.getMin(), 340);
    assertEquals(b.getMax(), 425);
    assertEquals(b.getSize(), 57418);


    }

  }
