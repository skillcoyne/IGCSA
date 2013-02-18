package org.lcsb.lu.igcsa.fasta;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import org.lcsb.lu.igcsa.utils.GenomeProperties;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAWriterTest
  {
  private File fastaFile;
  private FASTAWriter writer;


  @Before
  public void setUp() throws Exception
    {
    FASTAHeader header = new FASTAHeader(">gi|12345|This is a sample test case that illustrates FASTA writing");
    GenomeProperties props = GenomeProperties.readPropertiesFile("test.properties", GenomeProperties.GenomeType.NORMAL);

    fastaFile = new File(props.getProperty("dir.insilico") + "/test.fasta");
    assertFalse(fastaFile.exists());

    writer = new FASTAWriter(fastaFile, header);
    assertNotNull(writer);
    assertTrue(writer.getFASTAFile().exists());
    assertEquals("Header is written, file length should b 68", writer.getFASTAFile().length(), 68);
    }

  @After
  public void tearDown() throws Exception
    {
    writer.getFASTAFile().delete();
    new File(writer.getFASTAFile().getParent()).delete();
    assertFalse(writer.getFASTAFile().exists());
    writer.close();
    }

  @Test
  public void testWrite() throws Exception
    {
    long beforeWrite = writer.getFASTAFile().length();
    writer.writeLine("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    assertTrue("File should be longer after writing than before.", writer.getFASTAFile().length() > beforeWrite);
    }

  @Test
  public void testWriteLongString() throws Exception
    {
    String seq = "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" +
                 "XGCAGCAAAGAGTCAGCAAGAACACCGATAGGTACGTTTCCAGCTGCCTACGGACAGGGCGGCTCCCTAA" +
                 "XTATATATA";
    writer.writeLine(seq);
    assertEquals("With line separators this file should be length 220", writer.getFASTAFile().length(), 220);
    }

  }
