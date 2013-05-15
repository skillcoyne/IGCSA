package org.lcsb.lu.igcsa.fasta;

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
    assertEquals(writer.getFASTAFile().length(), header.getFormattedHeader().length());
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
                 "XTATATATA";
    writer.write(seq);
    writer.flush();

    FASTAReader reader = new FASTAReader(fastaFile);
    assertTrue( reader.readSequence(seq.length()).equals(seq) );

    assertEquals(writer.getFASTAFile().length(), header.getFormattedHeader().length()+seq.length()+3);
    }

  @Test
  public void testWriteLongSequence() throws Exception
    {
    writer.write(this.fastaSeq);
    writer.flush();

    FASTAReader reader = new FASTAReader(fastaFile);
    assertTrue( reader.readSequence(fastaSeq.length()).equals(fastaSeq) );
    }

  }
