/**
 * org.lcsb.lu.igcsa.dist
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
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class KaryotypeGeneratorTest
  {
  static Logger log = Logger.getLogger(KaryotypeGeneratorTest.class.getName());

  @Autowired
  private KaryotypeGenerator karyotypeGenerator;

  private Karyotype testKaryotype;

  @Before
  public void setUp() throws Exception
    {
    testKaryotype = new Karyotype();

    //File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));
//    testKaryotype.setGenomeDirectory(insilicoTest);
//    testKaryotype.setMutationDirectory(new File(insilicoTest, "structural-variations"));
    testKaryotype.setBuildName("Karyotype Test");
    testKaryotype.setKaryotypeDefinition(46, "XY");
    }


  @Test
  public void testGenerateKaryotype() throws Exception
    {
    karyotypeGenerator.generateKaryotypes(testKaryotype, new Band[]{new Band("2", "q11"), new Band("3", "p12"), new Band("6", "q32"), new Band("17", "p14")});

    log.info(testKaryotype.getAberrations());

    }
  }
