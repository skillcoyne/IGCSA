package org.lcsb.lu.igcsa.fasta;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAReaderTest
  {
  private FASTAReader reader;
  private File file;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("test.fasta");
    file = new File(testUrl.toURI());
    reader = new FASTAReader(file);
    assertNotNull(reader);
    assertEquals(reader.sequenceLength(), 654);
    }

  @Test
  public void testHeader() throws Exception
    {
    assertEquals( reader.getHeader().getClass(), FASTAHeader.class );
    }

  @Test
  public void testReadSequence() throws Exception
    {
    assertEquals(reader.readSequence(71, 81, true), "TGCAGCAAAG");
    assertEquals(reader.readSequence(1, 70, true), "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
    }

  @Test
  public void testRegions() throws Exception
    {
    assertNotNull(reader.getRepeatRegions());
    assertNotNull(reader.getGapRegions());

    assertEquals(reader.getRepeatRegions().length, 1);
    assertEquals(reader.getRepeatRegions()[0].getStart(), 69);
    assertEquals(reader.getRepeatRegions()[0].getEnd(), 69+70);

    assertEquals(reader.getGapRegions().length, 1);
    assertEquals(reader.getGapRegions()[0].getStart(), 586);
    assertEquals(reader.getGapRegions()[0].getEnd(), 600);
    }

  }
