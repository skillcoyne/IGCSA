package org.lcsb.lu.igcsa.fasta;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class FASTAReaderTest
  {
  @Autowired
  private Properties testProperties;

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

  @Test
  public void testZip() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/testzip.fa.gz");
    file = new File(testUrl.toURI());
    reader = new FASTAReader(file);
    assertNotNull(reader);

    int window = 100;
    int start = 0;
    String seq;
    while((seq = reader.readSequenceLength(start, window)) != null)
      {
      int end = (start+window > fastaSeq.length())? (fastaSeq.length()): (start+window);
      assertEquals(seq, fastaSeq.substring(start, end));
      start += window;
      }
    }


  @Test
  public void testHeader() throws Exception
    {
    assertEquals(reader.getHeader().getClass(), FASTAHeader.class);
    }

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
    assertEquals(buf.toString(), fastaSeq);
    assertEquals("Total sequence should be 644 characters", fastaSeq.length(), buf.toString().length());
    assertEquals(buf.toString(), fastaSeq);
    }

  @Test
  public void testReadFromLocation() throws Exception
    {
    String subSeq = fastaSeq.substring(0, 100);

    // This should allow me to retrieve the same sequence as many times as I like without advancing any read
    String seqA = reader.readSequenceLength(0, 100);
    assertEquals(seqA.length(), 100);
    assertEquals(seqA, subSeq);
    String seqB = reader.readSequenceLength(0, 100);
    assertEquals(seqA, seqB);

    String seqC = reader.readSequenceLength(45, 100);
    assertNotSame(seqA, seqC);
    assertEquals(seqC.length(), 100);
    }


  @Test
  public void testLoopRead() throws Exception
    {
    int window = 100;
    int start = 0;
    String seq;
    while((seq = reader.readSequenceLength(start, window)) != null)
      {
      int end = (start+window > fastaSeq.length())? (fastaSeq.length()): (start+window);
      assertEquals(seq, fastaSeq.substring(start, end));
      start += window;
      }
    }

  @Test
  public void testJumpLocations() throws Exception
    {
    long start = System.currentTimeMillis();
    String loc = "/Users/sarah.killcoyne/Data/FASTA/chr19.fa.gz";
    File file = new File(loc);
    FASTAReader reader = new FASTAReader(file);

    String myo9b = reader.readSequenceAtLocation(17186591, 17324104); // MYO9B gene

    String loc1 = "/Users/sarah.killcoyne/Downloads/myo9b.fasta";
    FASTAReader reader1 = new FASTAReader(new File(loc1));

    String ncbi_myo9b = reader1.readSequenceLength(0, 137513);

    char[] myoChar = myo9b.toCharArray();
    for (int i=0; i<myoChar.length; i++)
      {
      assertEquals(myo9b.charAt(i), ncbi_myo9b.charAt(i));
      }

    }

  }
