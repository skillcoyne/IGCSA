package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class BufferedFASTAReaderTest
  {
  static Logger log = Logger.getLogger(BufferedFASTAReaderTest.class.getName());

  private BufferedFASTAReader reader;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    File file = new File(testUrl.toURI());
    assertNotNull(file);

    reader = new BufferedFASTAReader(new InputStreamReader(new FileInputStream(file)));
    assertNotNull(reader);
    }

  @Test
  public void testInitialState() throws Exception
    {
    assertEquals(reader.getSequenceStartLocation(), 67);
    assertEquals(reader.getSequenceLineLength(), 70);
    assertEquals(reader.getCurrentSequenceLocation(), 67);
    }

  @Test
  public void testGetHeader() throws Exception
    {
    assertNotNull(reader.getHeader());
    assertEquals(reader.getHeader().getDB(), "gi");
    }

  @Test
  public void testRead() throws Exception
    {
    char c = (char) reader.read();
    assertSame(c, 'N');
    assertEquals(reader.getCurrentSequenceLocation(), 68);
    }

  @Test
  public void testSkip() throws Exception
    {
    assertEquals(reader.getCurrentSequenceLocation(), 67);
    reader.skip(71);
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 10; i++)
      {
      buf.append((char) reader.read());
      }

    assertEquals(buf.toString(), "TGCAGCAAAG");

    try
      {
      reader.skip(1);
      }
    catch (IOException e)
      {
      assertNotNull(e);
      }
    }


  }
