package org.lcsb.lu.igcsa.prob;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ProbabilityListTest
  {
  private ProbabilityList pl;

  @Before
  public void setUp() throws Exception
    {
    pl = new ProbabilityList();
    pl.add( new Probability("a", 0.3) );
    pl.add( new Probability("b", 0.2) );
    pl.add( new Probability("c", 0.5) );
    assertNotNull(pl);
    }

  @Test
  public void testCompareTo() throws Exception
    {
    Probability[] a = pl.toArray();
    Probability[] b = pl.sort();

    assertNotSame(a,b);
    assertEquals(b[0].getName(), "c");
    assertEquals(b[1].getName(), "a");
    assertEquals(b[2].getName(), "b");
    }

  @Test
  public void testIsSumOne() throws Exception
    {
    assertTrue(pl.isSumOne());
    }
  }
