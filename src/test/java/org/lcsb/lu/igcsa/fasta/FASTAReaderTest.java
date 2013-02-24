package org.lcsb.lu.igcsa.fasta;

import org.junit.After;
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



  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    file = new File(testUrl.toURI());
    assertNotNull(file);
    reader = new FASTAReader(file);
    assertNotNull(reader);
    }

  @After
  public void tearDown() throws Exception
    {

    }

  @Test
  public void testHeader() throws Exception
    {
    assertEquals(reader.getHeader().getClass(), FASTAHeader.class);
    }

//  @Test
//  public void testReadSequence() throws Exception
//    {
//    assertEquals(reader.readSequence(71, 81, true), "TGCAGCAAAG");
//    assertEquals(reader.readSequence(1, 70, true), "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
//    }

  @Test
  public void testRegions() throws Exception
    {
    reader.markRegions();
    assertNotNull(reader.getRepeatRegions());
    assertNotNull(reader.getGapRegions());

    assertEquals(reader.getRepeatRegions().length, 1);
    assertEquals(reader.getRepeatRegions()[0].getStart(), 69);
    assertEquals(reader.getRepeatRegions()[0].getEnd(), 69+70);

    assertEquals(reader.getGapRegions().length, 1);
    assertEquals(reader.getGapRegions()[0].getStart(), 586);
    assertEquals(reader.getGapRegions()[0].getEnd(), 600);
    }

  @Test
  public void testSequenceWindowRead() throws Exception
    {
    int window = 50;
    assertEquals("Sequence length should equal the window.", reader.readSequence(window).length(), window);
    assertTrue("File location after reading sequence should be advanced.", reader.getLastLocation() > window);
    }

  @Test
  public void testRepeatWindowRead() throws Exception
    {
    StringBuffer buf = new StringBuffer();
    int window = 100;
    String seq;
    while(true)
      {
      seq = reader.readSequence(window);
      buf.append(seq);
      if (seq.length() < window) break;
      }
    assertEquals("Total sequence should be 644 characters", 644, buf.toString().length());
    assertEquals(buf.toString(), fastaSeq);
    }

  @Test
  public void testReadFromLocation() throws Exception
    {
    String subSeq = fastaSeq.substring(0, 100);

    // This should allow me to retrieve the same sequence as many times as I like without advancing any read
    String seqA = reader.readSequenceFromLocation(0, 100);
    assertEquals(seqA.length(), 100);
    assertEquals(seqA, subSeq);
    String seqB = reader.readSequenceFromLocation(0, 100);
    assertEquals(seqA, seqB);

    String seqC = reader.readSequenceFromLocation(45, 100);
    assertNotSame(seqA, seqC);
    assertEquals(seqC.length(), 100);
    }


  @Test
  public void testLoopRead() throws Exception
    {
    int window = 100;
    int start = 0;
    String seq;
    while((seq = reader.readSequenceFromLocation(start, window)).length() > 0)
      {
      int end = (start+window > fastaSeq.length())? (fastaSeq.length()): (start+window);
      assertEquals(seq, fastaSeq.substring(start, end));
      start += window;
      }
    }


  }
