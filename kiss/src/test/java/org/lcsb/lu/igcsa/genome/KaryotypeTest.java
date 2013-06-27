package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.aberrations.Aberration;
import org.lcsb.lu.igcsa.aberrations.Addition;
import org.lcsb.lu.igcsa.aberrations.Deletion;
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
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class KaryotypeTest
  {
  static Logger log = Logger.getLogger(KaryotypeTest.class.getName());

  @Autowired
  private Karyotype testKaryotype;

  @Autowired
  private Properties ktProperties;

  @Autowired
  private Properties testProperties;

  private File fastaFile;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/chr21.fa.gz");
    fastaFile = new File(testUrl.toURI());


    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));
    testKaryotype.setGenomeDirectory(insilicoTest);
    testKaryotype.setMutationDirectory(new File(insilicoTest, "structural-variations"));
    testKaryotype.setBuildName("Karyotype Test");
    testKaryotype.setKaryotypeDefinition(46, "XY");
    assertEquals(testKaryotype.getPloidy(), 46);
    assertEquals(testKaryotype.getAllosomes(), "XY");
    }

  @After
  public void tearDown() throws Exception
    {

    }

  @Test
  public void testAddChromosome() throws Exception
    {
    for (String s : new String[]{"2", "5", "6", "X", "Y"})
      testKaryotype.addChromosome(new Chromosome(s));

    assertEquals(testKaryotype.getChromosomes().length, 5);
    assertEquals(testKaryotype.ploidyCount("X"), 1);
    }

  @Test
  public void testGainAneuploidy() throws Exception
    {
    testKaryotype.addChromosome(new Chromosome("6"));
    testKaryotype.chromosomeGain("6");
    assertEquals(testKaryotype.ploidyCount("6"), 3);
    }

  @Test
  public void testLoseAneuploidy() throws Exception
    {
    testKaryotype.chromosomeLoss("Y");
    assertEquals(testKaryotype.ploidyCount("Y"), 0);
    assertNull(testKaryotype.getChromosome("Y"));
    }

  @Test
  public void testAddAberration() throws Exception
    {
//    Aberration aberration = new Addition();
//    aberration.addFragment(new ChromosomeFragment("6", "p22", new Location(15200001, 30400000)));
//    testKaryotype.addAbberation(aberration);
//    assertEquals(testKaryotype.getAberrations().contains(aberration), aberration);
    }

  @Test
  public void testApplyAberrations() throws Exception
    {
//    testKaryotype.addChromosome(new Chromosome("21", fastaFile));
//    assertEquals(testKaryotype.getChromosome("21").getFASTA(), fastaFile);
//
//    // can't add overlapping aberrations of the same type
//    Aberration aberration = new Deletion();
//    aberration.addFragment(new ChromosomeFragment("21", "p12", new Location(2800001, 6800000)));
//    aberration.addFragment(new ChromosomeFragment("21", "q22", new Location(31500001, 48129895)));
//    aberration.addFragment(new ChromosomeFragment("21", "p12f", new Location(4800001, 6800000)));
//
//    testKaryotype.addAbberation(aberration);
//
//    assertEquals(testKaryotype.getAberrationByType(Deletion.class.getSimpleName()).getFragmentLocations().get("21").size(), 2);
//
//    testKaryotype.applyAberrations();
    }
  }
