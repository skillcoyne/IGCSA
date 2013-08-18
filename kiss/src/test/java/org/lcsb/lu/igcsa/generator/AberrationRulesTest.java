/**
 * org.lcsb.lu.igcsa.generator
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.Band;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.paukov.combinatorics.util.ComplexCombinationGenerator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class) @ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class AberrationRulesTest
  {
  static Logger log = Logger.getLogger(AberrationRulesTest.class.getName());

  private AberrationRules rules;

  @Before
  public void setUp() throws Exception
    {
    rules = new AberrationRules();
    }

  @Test
  public void testGetAberrationClasses() throws Exception
    {
    assertEquals(rules.getAberrationClasses().length, 7);
    }


  @Test
  public void testAberrationsByBreakpoint() throws Exception
    {
    Band[] bands = new Band[]{new Band("2", "q11"), new Band("3", "p12"), new Band("6", "q32"), new Band("17", "p14")};
    rules.applyRules(bands);

    assertEquals(rules.getOrderedBreakpointSets().size(), rules.getBreakpointSets().size());
    }


  /*
  Single chromosome tests
  1)	Where there is only a single chromosomes with breakpoints
    a)	If there is a single bp and it is a centromere the aberration is ISO, DEL, or DUP
    b)	If there are 2 centromeric bands the aberration is DIC, DEL, DUP, INS, or INV
    c)	If there is a one or more arm bp it can be a DEL, DUP, INS, or INV
  */
  @Test
  public void test1A() throws Exception
    {
    Band[] bands = new Band[]{new Band("2", "q11")};
    rules.applyRules(bands);
    assertEquals(rules.getAberrations().size(), 3); // ISO, DUP, DEL
    }

  @Test
  public void test1B() throws Exception
    {
    Band[] bands = new Band[]{new Band("2", "q11"), new Band("2", "p12")};
    rules.applyRules(bands);
    assertEquals(rules.getAberrations().size(), AberrationRules.TWO_CENTROMERES.length + (AberrationRules.SINGLE_CENTROMERE.length * 2)); // DIC, DEL, DUP, INS, INV
    }

  @Test
  public void test1C() throws Exception // centromere and arms
  {
  Band[] bands = new Band[]{new Band("22", "q11"), new Band("22", "p31"), new Band("22", "q42")};
  rules.applyRules(bands);

  int totalAberrations = (bands.length * AberrationRules.ONE_CHROMOSOME.length) + (AberrationRules.SINGLE_CENTROMERE.length) + (AberrationRules.ONE_CHROMOSOME.length * 2);
  assertEquals(rules.getAberrations().size(), totalAberrations);
  }

  /*
  Multiple chromosome tests
  2) Where there are multiple chromosomes with breakpoints
    a)	DIC is possible between 2 chromsomes with centromeric bands
    b)	TRANS are possible between 2+ chromosomes at any band
    c)	DEL, DUP, INS, or INV are possible in breakpoints within a single chromosome
  */
  @Test
  public void test2A() throws Exception
    {
    Band[] bands = new Band[]{new Band("2", "q11"), new Band("3", "p12")};
    rules.applyRules(bands);
    int totalAberrations = (bands.length * AberrationRules.SINGLE_CENTROMERE.length) + AberrationRules.MULTI_CHROMOSOME.length;
    assertEquals(rules.getAberrations().size(), totalAberrations);
    }

  @Test
  public void test2BC() throws Exception
    {
    Band[] bands = new Band[]{new Band("2", "q11"), new Band("3", "p12"), new Band("6", "q32"), new Band("17", "p14")};
    rules.applyRules(bands);

    int total = (AberrationRules.MULTI_CHROMOSOME.length * 6) + (AberrationRules.SINGLE_CENTROMERE.length * 2) + (AberrationRules.ONE_CHROMOSOME.length * 2);
    assertEquals(rules.getAberrations().size(), total);
    }

  @Test
  public void test2BCSameChr() throws Exception
    {
    Band[] bands = new Band[]{new Band("2", "q11"), new Band("3", "p22"), new Band("6", "q32"), new Band("2", "p16")};
    rules.applyRules(bands);

    int total = (AberrationRules.MULTI_CHROMOSOME.length * 5) + (AberrationRules.SINGLE_CENTROMERE.length) + (AberrationRules.ONE_CHROMOSOME.length * 3) + (AberrationRules.ONE_CHROMOSOME.length);
    assertEquals(rules.getAberrations().size(), total);
    }

//  @Test
//  public void testNullBand() throws Exception
//    {
//    Band[] bands = new Band[]{new Band("2", "q11"), new Band(null, null), new Band("6", "q32"), new Band("2", "p16")};
//    rules.applyRules(bands);
//
//
//    }

  }
