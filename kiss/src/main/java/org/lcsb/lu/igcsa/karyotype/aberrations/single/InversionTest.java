package org.lcsb.lu.igcsa.karyotype.aberrations.single;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.genome.Band;
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
public class InversionTest
  {
  static Logger log = Logger.getLogger(InversionTest.class.getName());

  private Inversion abr;
  private File fastaFile;

  private FASTAWriter writer;

  @Autowired
  private Properties testProperties;


  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/wiki-text.fa");
    fastaFile = new File(testUrl.toURI());

    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));
    writer = new FASTAWriter(new File(insilicoTest, "inv-test.fa"), new FASTAHeader("test", "inv", "1", "none"));

    abr = new Inversion();
    assertNotNull(abr);
    }

  @After
  public void tearDown() throws Exception
    {
    //writer.getFASTAFile().delete();
    }


  @Test
  public void testApplyAndWriteChromosome() throws Exception
    {
    Chromosome chr = new Chromosome("test", fastaFile);
    DerivativeChromosome dchr = new DerivativeChromosome("test", chr);

    abr = new Inversion();
    assertNotNull(abr);

    FASTAReader reader = new FASTAReader(fastaFile);
    assertEquals("viruses", reader.readSequenceFromLocation(136, 7));
    assertEquals("allknown", reader.readSequenceFromLocation(217, 8));

    abr.addFragment( new Band(chr.getName(), "viruses", new Location(136, 143)));
    abr.addFragment( new Band(chr.getName(), "allknown", new Location(217, 225)));
    abr.applyAberrations(dchr, writer, null);

    assertTrue(writer.getFASTAFile().length() > 17);
    FASTAReader newreader = new FASTAReader(writer.getFASTAFile());

    assertEquals("viruses", new StringBuffer(newreader.readSequenceFromLocation(136, 7)).reverse().toString());
    assertEquals("allknown", new StringBuffer(newreader.readSequenceFromLocation(217, 8)).reverse().toString());
    }

  @Test
  public void testLongFragments() throws Exception
    {
    Chromosome chr = new Chromosome("test", fastaFile);
    DerivativeChromosome dchr = new DerivativeChromosome("test", chr);

    abr = new Inversion();
    assertNotNull(abr);

    int startLoc = 144;

    FASTAReader reader = new FASTAReader(fastaFile);
    String origLongText = reader.readSequenceFromLocation(startLoc, 1103);
    assertEquals(origLongText.length(), 1103);

    abr.addFragment(new Band(chr.getName(), "longtext", new Location(startLoc, startLoc+origLongText.length()+1)));
    abr.applyAberrations(dchr, writer, null);

    assertTrue(writer.getFASTAFile().length() > 17);
    FASTAReader newreader = new FASTAReader(writer.getFASTAFile());

    assertNotSame(fastaFile, writer.getFASTAFile());
    assertNotSame(reader, newreader);

    assertEquals(origLongText, new StringBuffer(newreader.readSequenceFromLocation(startLoc, origLongText.length())).reverse().toString());
    }
  }

