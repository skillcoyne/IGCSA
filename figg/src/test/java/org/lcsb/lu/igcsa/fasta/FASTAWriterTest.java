package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Properties;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class FASTAWriterTest
  {
  static Logger log = Logger.getLogger(FASTAWriterTest.class.getName());

  private File fastaFile;
  private FASTAWriter writer;

  private String fastaSeq =
      "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" +
          "TGCAGCAAAGAGTCAGCAAGAACACCGATAGGTACGTTTCCAGCTGCCTACGGACAGGGCGGCTCCCTAA" +
          "GGTCTGGGTCAAACACATACAATAGTCGCAGAAGAGAACTAAGCAGCACGCTATCTGACCGCCGTAGCGC" +
          "CATCAGAGTAGTGGAGCCTAATGCCCTCAATTAGAGAGCGATAACCGGACTGCCCTACGCTAGGGCATAC" +
          "GTCGCCATTTTAGCGTGATGACGCAGTGGATCTGACTTTGTGTCCGAGGGTCCAGAAGGGAGGGCTAGCT" +
          "GTGCAATAGTGTTCGGTTTGGTAACGAGTCCTACCTCCGTACCATGCATGCTGACTACACAGGAACGTTT" +
          "AATTAGCCCGGGCATCGAATCCAACCAGGAGCGATAGTCGCCCTGAGTTCCGACCTGCTTGTCACACCTA" +
          "AATTAGCCCGGGCATCGAAT--------------CCAACCAGGAGCGATAGTCGCCCTGAGTTCCGACCT" +
          "GTCGCCATTTTAGCGTGATGACGCAGTGGATCTGACTTTGTGTCCGAGGGTCCAGAAGGGAGGGCTAGCT" +
          "AGGGAGGGCTAGCT";

  private FASTAHeader header;

  @Autowired
  private Properties testProperties;

  @Before
  public void setUp() throws Exception
    {
    fastaFile = new File(testProperties.getProperty("dir.insilico") + "/test.fasta");

    header = new FASTAHeader("figg", "12345", null, "This is a sample test case that illustrates FASTA writing");

    writer = new FASTAWriter(fastaFile, header);
    assertNotNull(writer);
    assertTrue(writer.getFASTAFile().exists());
    assertEquals(writer.getFASTAFile().length(), header.getFormattedHeader().length()+1);
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
    writer.write("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    writer.flush();
    assertTrue("File should be longer after writing than before.", writer.getFASTAFile().length() > beforeWrite);
    }

  @Test
  public void testWriteLongString() throws Exception
    {
    String seq = "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" +
                 "XGCAGCAAAGAGTCAGCAAGAACACCGATAGGTACGTTTCCAGCTGCCTACGGACAGGGCGGCTCCCTAA" +
                 "XTATATATAX";
    writer.write(seq);
    writer.flush();

    FASTAReader reader = new FASTAReader(fastaFile);

    assertTrue( reader.readSequence(seq.length()).equals(seq) );
    }

  @Test
  public void testWriteLongSequence() throws Exception
    {
    writer.write(this.fastaSeq);
    writer.flush();

    FASTAReader reader = new FASTAReader(fastaFile);
    assertTrue( reader.readSequence(fastaSeq.length()).equals(fastaSeq) );
    }

  @Test
  public void testBuffer() throws Exception
    {
    String rep = "#" + repeat("GC", 34) + "*";
    assertEquals(rep.length(), 70);

    for (int i=0; i<100; i++)
      writer.write(rep);
    writer.flush();

    FASTAReader reader = new FASTAReader(fastaFile);
    int lines = 0;
    String seq;
    while(true)
      {
      seq = reader.readSequence(rep.length());
      if (seq == null || seq.length() < rep.length()) break;
      assertEquals(seq, rep);
      ++lines;
      }
    assertEquals(lines, 100);
    }

  private static String repeat(String s, int times)
    {
    if (times <= 0) return "";
    else return s + repeat(s, times-1);
    }


  }
