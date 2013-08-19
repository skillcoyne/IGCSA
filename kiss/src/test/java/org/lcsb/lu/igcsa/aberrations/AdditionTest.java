package org.lcsb.lu.igcsa.aberrations;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.aberrations.single.Addition;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class AdditionTest
  {
  static Logger log = Logger.getLogger(AdditionTest.class.getName());

  private Addition abr;
  private File fastaFile;

  private File dir;
  private FASTAWriter writer;

  @Autowired
  private Properties testProperties;


  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/seq.fa");
    fastaFile = new File(testUrl.toURI());

    dir = new File(testProperties.getProperty("dir.karyotype"));
    dir.mkdirs();

    writer = new FASTAWriter(new File(dir, "add-test.fa"), new FASTAHeader("test", "add", "1", "none"));

    abr = new Addition();
    assertNotNull(abr);
    }

  @After
  public void tearDown() throws Exception
    {
    writer.getFASTAFile().delete();
    FileUtils.deleteDirectory(dir);
    }


  @Test
  public void testaddFragment() throws Exception
    {
    abr.addFragment(new Band("6", "p22", new Location(15200001, 30400000)));
    assertEquals(abr.getLocationsForChromosome(new Chromosome("6")).size(), 1);
    }

  @Test
  public void testApplyAberrations() throws Exception
    {
    Chromosome chr = new Chromosome("test", fastaFile);
    DerivativeChromosome dchr = new DerivativeChromosome("test", chr);

    abr.addFragment(new Band("test", "XARM", new Location(1136, 1988)));
    abr.applyAberrations(dchr, writer, null);

    FASTAReader reader = new FASTAReader(writer.getFASTAFile());
    String str = reader.readSequenceFromLocation(1989, 852);
    for (char c: str.toCharArray())
      assertEquals(c, 'N');
    }
  }
