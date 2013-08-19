package org.lcsb.lu.igcsa.aberrations.multiple;

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
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
@RunWith (SpringJUnit4ClassRunner.class) @ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class TranslocationTest
  {
  static Logger log = Logger.getLogger(TranslocationTest.class.getName());

  private FASTAWriter writer;

  private File fastaOne;
  private File fastaTwo;

  private Chromosome chr1;
  private Chromosome chr2;

  @Autowired
  private Properties testProperties;


  @Before
  public void setUp() throws Exception
    {
    fastaOne = new File(ClassLoader.getSystemResource("fasta/wiki-text.fa").toURI());
    fastaTwo = new File(ClassLoader.getSystemResource("fasta/wiki-text2.fa").toURI());

    chr1 = new Chromosome("dna", fastaOne);
    chr2 = new Chromosome("global-warming", fastaTwo);

    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));
    writer = new FASTAWriter(new File(insilicoTest, "trans-test.fa"), new FASTAHeader("test", "trans", "1", "none"));
    }

  @After
  public void tearDown() throws Exception
    {
    writer.getFASTAFile().delete();
    }

  @Test
  public void testAddFragment() throws Exception
    {
    Translocation abr = new Translocation();

    abr.addFragment(new Band(chr1.getName(), "dna", new Location(144, 608)), chr1);
    abr.addFragment(new Band(chr2.getName(), "climate", new Location(702, 1064)), chr2);
    abr.addFragment(new Band(chr1.getName(), "dna", new Location(0, 20)), chr1);
    abr.addFragment(new Band(chr2.getName(), "climate", new Location(993, 1023)), chr2);

    assertEquals(abr.getFragments().size(), 4);
    }

  @Test
  public void testApplyAberrations() throws Exception
    {
    Translocation abr = new Translocation();

    Band[] bands = new Band[]{
        new Band(chr1.getName(), "dna", new Location(144, 607)),
        new Band(chr2.getName(), "climate", new Location(702, 1063)),
        new Band(chr1.getName(), "dna", new Location(0, 19)),
        new Band(chr2.getName(), "climate", new Location(993, 1067))};

    abr.addFragment(bands[0], chr1);
    abr.addFragment(bands[1], chr2);
    abr.addFragment(bands[2], chr1);
    abr.addFragment(bands[3], chr2);

    abr.applyAberrations(new DerivativeChromosome("derivative"), writer, null);
    assertTrue(writer.getFASTAFile().length() > 20);

    FASTAReader reader = new FASTAReader(writer.getFASTAFile());

    assertEquals("Beginning to first band", reader.readSequenceFromLocation(0, bands[0].getLocation().getStart()), chr1.getFASTAReader().readSequenceFromLocation(0, bands[0].getLocation().getStart()));

    assertEquals("First band", reader.readSequenceFromLocation(bands[0].getLocation().getStart(), bands[0].getLocation().getLength() + 1), chr1.getFASTAReader().readSequenceFromLocation(bands[0].getLocation().getStart(), bands[0].getLocation().getLength() + 1));

    assertEquals("Second band", reader.readSequenceFromLocation(bands[0].getLocation().getEnd() + 1, bands[1].getLocation().getLength() + 1), chr2.getFASTAReader().readSequenceFromLocation(bands[1].getLocation().getStart(), bands[1].getLocation().getLength() + 1));

    int lastLoc = bands[0].getLocation().getEnd() + 1 + bands[1].getLocation().getLength() + 1;
    assertEquals("Third band", reader.readSequenceFromLocation(lastLoc + 1, bands[2].getLocation().getLength() + 1), chr1.getFASTAReader().readSequenceFromLocation(bands[2].getLocation().getStart(), bands[2].getLocation().getLength() + 1));

    lastLoc = lastLoc +1 + bands[2].getLocation().getEnd();
    assertEquals("Fourth band", reader.readSequenceFromLocation(lastLoc, bands[3].getLocation().getLength()+1),
        chr2.getFASTAReader().readSequenceFromLocation(bands[3].getLocation().getStart(), bands[3].getLocation().getLength()+1));

    assertEquals("Remainder", reader.readSequence(1000), chr2.getFASTAReader().readSequenceFromLocation(bands[3].getLocation().getEnd()+1, 1000));
    }
  }

