package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:kiss-test-spring-config.xml"})
public class KaryotypeTest
  {
  static Logger log = Logger.getLogger(KaryotypeTest.class.getName());

  @Autowired
  private Karyotype testKaryotype;

  @Autowired
  private Properties ktProperties;

  @Autowired
  private Properties testProperties;


  @Before
  public void setUp() throws Exception
    {
//    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
//    File fastaFile = new File(testUrl.toURI());


    File insilicoTest = new File(testProperties.getProperty("dir.insilico"));
    testKaryotype.setGenomeDirectory(insilicoTest);
    testKaryotype.setMutationDirectory(new File(insilicoTest, "structural-variations"));

    testKaryotype.setKaryotypeDefinition(46, "XY");

    for (String s: new String[]{"2", "5", "6", "X", "Y"})
      testKaryotype.addChromosome(new Chromosome(s));

    assertEquals(testKaryotype.getChromosomes().length, 5);
    }

  @After
  public void tearDown() throws Exception
    {

    }

  @Test
  public void testGainAneuploidy() throws Exception
    {
    testKaryotype.chromosomeGain("6");
    assertEquals(testKaryotype.ploidyCount("6"), 3);
    }

  @Test
  public void testLoseAneuploidy() throws Exception
    {
    testKaryotype.chromosomeLoss("Y");
    assertEquals(testKaryotype.ploidyCount("Y"), 0);

    //assertNull(testKaryotype.getChromosome("Y"));

    }

  @Test
  public void testCreateAberration() throws Exception
    {

    }
  }
