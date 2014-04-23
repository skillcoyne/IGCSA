/**
 * org.lcsb.lu.igcsa.database.sql
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.database;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.prob.Probability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class) @ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class JDBCGeneralProbabilityDAOTest
  {
  static Logger log = Logger.getLogger(JDBCGeneralProbabilityDAOTest.class.getName());

  @Autowired
  private KaryotypeDAO dao;


  @Before
  public void setUp() throws Exception
    {

    }

  @Test
  public void testXBandError() throws Exception
    {

    Probability p = dao.getGeneralKarytoypeDAO().getBandProbabilities("X");

    log.info(p.getProbabilities());

    String b = (String) p.roll();
    assertTrue( b.matches("(p|q)\\d+") );
    log.info(b);

    Band band = dao.getBandDAO().getBandByChromosomeAndName("X",b);
    assertNotNull(band);
    }
  }
