package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.aberrations.single.Deletion;
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
public class DeletionTest
  {
  static Logger log = Logger.getLogger(DeletionTest.class.getName());

  private Deletion abr;
  private File fastaFile;

  private FASTAWriter writer;

  @Autowired
  private Properties testProperties;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/seq.fa");
    fastaFile = new File(testUrl.toURI());
    log.info("file length " + fastaFile.length());

    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));

    writer = new FASTAWriter(new File(insilicoTest, "del-test.fa"), new FASTAHeader("test", "del", "1", "none"));

    abr = new Deletion();
    assertNotNull(abr);
    }

  @After
  public void tearDown() throws Exception
    {
    writer.getFASTAFile().delete();
    }

  @Test
  public void testApplyAberrations() throws Exception
    {
    Chromosome chr = new Chromosome("test", fastaFile);
    DerivativeChromosome dchr = new DerivativeChromosome("test", chr);

    abr.addFragment(new Band("test", "XARM", new Location(980, 1050)));
    abr.addFragment(new Band("test", "XARM", new Location(4332, 4341)));

    abr.applyAberrations(dchr, writer, null);

    FASTAReader reader = new FASTAReader(writer.getFASTAFile());

    // no ---'s
    assertEquals(reader.readSequenceFromLocation(978, 8), "CZZZZZZA");

    // no XX's
    assertFalse(reader.readSequenceFromLocation(4260, 100).contains("X"));
    }

  @Test
  public void testApplyBadAbberation() throws Exception
    {
    //This location is beyond the end of the file
    abr.addFragment(new Band("test", "XARM", new Location(4464, 4473)));
    try
      {
      Chromosome chr = new Chromosome("test", fastaFile);
      abr.applyAberrations(new DerivativeChromosome("test", chr), writer, null);
      }
    catch (IllegalArgumentException ae)
      {
      assertNotNull(ae);
      }
    }

  @Test
  public void testAddFragment() throws Exception
    {
    // only one will be added because I do not currently allow for overlapping locations.  Each aberration could possibly
    // handle this specially on it's own in the future though
    abr.addFragment(new Band("6", "p22", new Location(15200001, 30400000)));
    abr.addFragment(new Band("6", "p22.1", new Location(15500000, 30400000)));
    assertEquals(abr.getLocationsForChromosome(new Chromosome("6")).size(), 1);
    }
  }
