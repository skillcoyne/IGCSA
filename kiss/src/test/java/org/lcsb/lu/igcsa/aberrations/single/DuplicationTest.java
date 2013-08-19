package org.lcsb.lu.igcsa.aberrations.single;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
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
public class DuplicationTest
  {
  static Logger log = Logger.getLogger(DuplicationTest.class.getName());

  private File fastaFile;
  private FASTAWriter writer;
  private Duplication dup;

  @Autowired
  private Properties testProperties;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/wiki-text.fa");
    fastaFile = new File(testUrl.toURI());
    log.info("file length " + fastaFile.length());

    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));

    writer = new FASTAWriter(new File(insilicoTest, "dup-test.fa"), new FASTAHeader("test", "dup", "1", "none"));

    dup = new Duplication();
    assertNotNull(dup);
    }

  @After
  public void tearDown() throws Exception
    {
    //writer.getFASTAFile().delete();
    }

  @Test
  public void testAddFragments() throws Exception
    {
    dup.addFragment(new Band("test", "nerfin", new Location(500, 38928)));

    try
      {
      dup.addFragment(new Band("test", "noway", new Location(493, 999)));
      }
    catch (IllegalArgumentException ae)
      {
      assertNotNull(ae);
      }
    }

  @Test
  public void testApplyAberrations() throws Exception
    {
    Chromosome chr = new Chromosome("test", fastaFile);
    DerivativeChromosome dchr = new DerivativeChromosome("test", chr);

    dup.addFragment(new Band(chr.getName(), "dna", new Location(144, 608)));

    dup.applyAberrations(dchr, writer, null);

    FASTAReader reader = new FASTAReader(writer.getFASTAFile());
    assertEquals(chr.getFASTAReader().readSequenceFromLocation(144, 464), reader.readSequenceFromLocation(609, 464));
    }
  }
