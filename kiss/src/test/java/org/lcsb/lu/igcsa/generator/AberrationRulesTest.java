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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
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
  public void testSingleChr() throws Exception
    {
    Band[] bands = new Band[]{new Band("22","q11"), new Band("22","p31"), new Band("22","q42")};
    rules.applyRules(bands);
    }

  @Test
  public void testSingleChrCentromereOnly() throws Exception
    {
    Band[] bands = new Band[]{new Band("2","q11")};
    rules.applyRules(bands);
    assertEquals(rules.getAberrations().size(), 1);
    }

  @Test
  public void testSingleChrCentromeresOnly() throws Exception
    {
    Band[] bands = new Band[]{new Band("2","q11"), new Band("2", "p12")};
    rules.applyRules(bands);
    assertEquals(rules.getAberrations().size(), 3);
    }

  @Test
  public void testApplyRules() throws Exception
    {
    rules.setAberrationClasses(new Object[]{"inv", "del", "trans"});

    Band[] bands = new Band[]{new Band("22","q11"), new Band("12","p31"), new Band("3","q42")};
    rules.applyRules(bands);
    assertEquals(rules.getAberrations().size(), rules.getAberrationClasses().length*bands.length);
    }
  }
