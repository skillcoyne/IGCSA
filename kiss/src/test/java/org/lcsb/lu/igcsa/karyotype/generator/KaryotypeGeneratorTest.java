/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.generator;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.karyotype.database.Band;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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

    URL testUrl = ClassLoader.getSystemResource("fasta/wiki-text.fa");
    File fastaFile = new File(testUrl.toURI());

    testKaryotype.setBuildName("Karyotype Test");
    testKaryotype.setKaryotypeDefinition(46, "XY");

    for (int c = 1; c <= 22; c++)
      testKaryotype.addChromosome(new Chromosome(Integer.toString(c), fastaFile));
    }

  @Test
  public void testGenerateWithOneBand() throws Exception
    {
    Band[] bands = new Band[]{new Band("2", "q11", new Location(1, 40))};

    Karyotype k = karyotypeGenerator.generateKaryotype(testKaryotype, bands);
    assertEquals(k.getDerivativeChromosomes().length, 1);
    }


  @Test
  public void testGenerateKaryotypeWithKnownBands() throws Exception
    {
    List<Karyotype> karyotypes = new ArrayList<Karyotype>();

    for (int i = 0; i < 3; i++)
      {
      karyotypes.add(karyotypeGenerator.generateKaryotype(testKaryotype,
          new Band[]{new Band("2", "q11", new Location(1, 40)),
          new Band("3", "p12", new Location(1, 40)),
          new Band("6", "q32", new Location(1, 40)),
          new Band("17", "p14", new Location(1,
                                                                                                                                   40))}));
      }

    for (Karyotype k : karyotypes)
      {
//      log.info(k.getAberrationDefinitions().size());
//      log.info(k.getAberrationDefinitions());
      assertTrue(k.getAberrationDefinitions().size() > 0);
//      assertTrue(k.getAberrationDefinitions().size() <= 6 && k.getAberrationDefinitions().size() > 0);
      assertEquals(k.getChromosomeCount("X"), 1);
      assertEquals(k.getChromosomeCount("Y"), 1);
      }
    }


  @Test
  public void testNoBandError() throws Exception
    {
    karyotypeGenerator.generateKaryotype(testKaryotype, new Band[]{new Band("2", "", new Location(1, 40))} );
//            new Band("3", "p12", new Location(1, 40)),
//            new Band("6", "q32", new Location(1, 40)),
//            new Band("17", "p14", new Location(1,

        }
  }
