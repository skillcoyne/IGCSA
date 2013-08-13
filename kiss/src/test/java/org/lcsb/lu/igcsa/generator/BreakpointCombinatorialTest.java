/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.Band;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class BreakpointCombinatorialTest
  {
  static Logger log = Logger.getLogger(BreakpointCombinatorialTest.class.getName());

  Band[] bands = new Band[]{new Band("22","q11"), new Band("12","p31"), new Band("3","q42")};

  Object[] objects = new String[]{"inv", "dup", "del", "ins"};


  @Test
  public void testSimple() throws Exception
    {
    BreakpointCombinatorial.GENERATOR = BreakpointCombinatorial.SIMPLE_GEN;
    List<ICombinatoricsVector<Band>> comb = new BreakpointCombinatorial().getCombinations(bands, 2);
    assertEquals(comb.size(), 3);

    }

  @Test
  public void testGetCombinations() throws Exception
    {
    BreakpointCombinatorial.GENERATOR = BreakpointCombinatorial.MULTI_GEN;

    List<ICombinatoricsVector<Band>> comb = new BreakpointCombinatorial().getCombinations(bands, 2);
    assertEquals(comb.size(), 6);
    }


  @Test
  public void testGetAberrationCombination() throws Exception
    {
    BreakpointCombinatorial.GENERATOR = BreakpointCombinatorial.MULTI_GEN;

    List<ICombinatoricsVector<Aberration>> comb = new BreakpointCombinatorial().getAberrationCombination(objects, bands, 2);
    assertEquals(comb.size(), 6*objects.length);
    }


  }
