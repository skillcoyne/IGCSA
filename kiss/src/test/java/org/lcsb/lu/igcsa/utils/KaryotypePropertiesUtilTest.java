package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.aberrations.Aberration;
import org.lcsb.lu.igcsa.database.ChromosomeBandDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:kiss-test-spring-config.xml"})
public class KaryotypePropertiesUtilTest
  {
  static Logger log = Logger.getLogger(KaryotypePropertiesUtilTest.class.getName());

  @Autowired
  private Properties ktProperties;

  @Autowired
  private ChromosomeBandDAO testBandDAO;

  @Before
  public void setUp() throws Exception
    {
    for (String s: new String[]{"ploidy", "sex", "gain", "loss"})
      ktProperties.remove(s);
    }


  @Test
  public void testGetAberrationList() throws Exception
    {
    List<Aberration> abrList = KaryotypePropertiesUtil.getAberrationList(testBandDAO, ktProperties);
    assertEquals(abrList.size(), 3);
    }
  }
