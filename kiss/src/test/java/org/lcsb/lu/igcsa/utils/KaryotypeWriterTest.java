package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.aberrations.single.Deletion;
import org.lcsb.lu.igcsa.aberrations.single.Inversion;
import org.lcsb.lu.igcsa.aberrations.single.SingleChromosomeAberration;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.Properties;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class KaryotypeWriterTest
  {
  static Logger log = Logger.getLogger(KaryotypeWriterTest.class.getName());

  private Karyotype kt;

  @Autowired
  private Properties testProperties;

  private File outFile;

  @Before
  public void setUp() throws Exception
    {
    outFile = new File(testProperties.getProperty("dir.tmp"), "karyotype.txt");

    URL testUrl = ClassLoader.getSystemResource("fasta/seq.fa");
    File fastaFile = new File(testUrl.toURI());

    kt = new Karyotype();

    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));
    kt.setGenomeDirectory(insilicoTest);
    kt.setMutationDirectory(new File(insilicoTest, "structural-variations"));
    kt.setBuildName("Karyotype Test");
    kt.setKaryotypeDefinition(46, "XY");

    for (String s : new String[]{"2", "5", "6", "X", "Y"})
      kt.addChromosome(new Chromosome(s));

    kt.gainChromosome("X");
    kt.loseChromosome("Y");

    DerivativeChromosome dchr = new DerivativeChromosome("5");
    SingleChromosomeAberration abr = new Deletion();
    abr.addFragment(new Band("5", "p13", new Location(1, 10)));
    abr.addFragment(new Band("5", "q23", new Location(145, 680)));
    dchr.addAberration(abr);

    abr = new Inversion();
    abr.addFragment(new Band("5", "p23", new Location(145, 680)));
    abr.addFragment(new Band("5", "q34", new Location(900, 1680)));
    dchr.addAberration(abr);

    kt.addDerivativeChromosome(dchr);
    }

  @After
  public void tearDown() throws Exception
    {
    //outFile.delete();
    }


  @Test
  public void testWrite() throws Exception
    {

    KaryotypeWriter writer = new KaryotypeWriter( kt, outFile );
    writer.write();

    }
  }
