package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;
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
  static Logger log = Logger.getLogger(FASTAReaderTest.class.getName());

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
    while((seq = reader.readSequence(window)) != null)
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
  public void testLoopRead() throws Exception
    {
    int window = 100;
    int start = 0;
    String seq;
    while((seq = reader.readSequence(window)) != null)
      {
      int end = (start+window > fastaSeq.length())? (fastaSeq.length()): (start+window);
      assertEquals(seq, fastaSeq.substring(start, end));
      start += window;
      }
    }

  @Test
  public void testReadFromLocation() throws Exception
    {
    assertEquals(reader.readSequenceFromLocation(1, 10), "NNNNNNNNNN");
    // making sure that sequential reads don't overlap
    assertEquals(reader.readSequenceFromLocation(71, 10), "TGCAGCAAAG");
    assertEquals(reader.readSequenceFromLocation(81, 10), "AGTCAGCAAG");
    assertEquals(reader.readSequenceFromLocation(113, 10), "GCTGCCTACG");

    // make sure that I can jump back to a previous read location
    assertEquals(reader.readSequenceFromLocation(71, 10), "TGCAGCAAAG");
    }


  @Test
  public void testSkip() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/wiki-text.fa");
    FASTAReader reader = new FASTAReader(new File(testUrl.toURI()));
    assertNotNull(reader);

    assertEquals(reader.readSequenceFromLocation(1, 20), "Deoxyribonucleicacid");
    assertEquals(reader.readSequenceFromLocation(21, 5), "(DNA)");
    assertEquals(reader.readSequenceFromLocation(51, 7), "genetic");
    assertEquals(reader.readSequenceFromLocation(144, 12), "AlongwithRNA");

    //jump back to previous read location
    assertEquals(reader.readSequenceFromLocation(21, 5), "(DNA)");
    assertEquals(reader.readSequenceFromLocation(371, 15), "double-stranded");
    assertEquals(reader.readSequenceFromLocation(120, 9), "organisms");
    }

  @Test
  public void testStreamToWriter() throws Exception
    {
    File fastaFile = new File(testProperties.getProperty("dir.insilico") + "/test.fasta");
    FASTAHeader header = new FASTAHeader("figg", "12345", null, "This is a sample test case that illustrates FASTA stream read/write");

    int charsToRead = 23;
    assertEquals(reader.streamToWriter(charsToRead, new FASTAWriter(fastaFile, header)), charsToRead);

    charsToRead = 114;
    assertEquals(reader.streamToWriter(charsToRead, new FASTAWriter(fastaFile, header)), charsToRead);

    assertEquals(reader.streamToWriter(140, 213, new FASTAWriter(fastaFile, header)), 213-140);

    fastaFile.delete();
    fastaFile.getParentFile().delete();
    }

  @Test
  public void testSequentialReadStarting() throws Exception
    {
    int start = 71;
    int end = 113;
    int seqLength = end - start;
    int window = 10;

    String sequence = reader.readSequenceFromLocation(start, window);
    assertEquals(sequence, "TGCAGCAAAG");
    while (sequence.length() < seqLength)
      {
      String currSeq = reader.readSequence(window);
      sequence = sequence + currSeq;
      if (seqLength - sequence.length() < window ) window = seqLength - sequence.length();
      }

    assertEquals(sequence.length(), seqLength);
    assertEquals(sequence, "TGCAGCAAAGAGTCAGCAAGAACACCGATAGGTACGTTTCCA");
    }



//  @Test
//  public void testJumpLocations() throws Exception
//    {
//    String loc = "/Users/sarah.killcoyne/Data/FASTA/tmp/chr19.fa";
//    File file = new File(loc);
//    FASTAReader reader = new FASTAReader(file);
//
//    //String myo9b = "";// = reader.readSequenceAtLocation(17186591, 17324104); // MYO9B gene
//
//    int start = 17186591;
//    int end = 17324104;
//    int seqLength = end-start;
//    int window = 100;
//    String myo9b = reader.readSequenceFromLocation(start, window);
//
//    assertEquals(myo9b, "CGGGGCGGAGCGGCTCGAGCAGCGGCGGGCTGGCAGGCGGTCGTCCGGCCGGGGACCCGGCCCGGGACCGGCGGCGCGCGGCGGCCGAGGCCAGGTGAGT");
//
//    while( myo9b.length() < seqLength)
//      {
//      String seq = reader.readSequence(window);
//      myo9b = myo9b + seq;
//      if (seqLength - myo9b.length() < window ) window = seqLength - myo9b.length();
//      }
//
//    assertEquals(myo9b.length(), seqLength);
//
//    String loc1 = "/Users/sarah.killcoyne/Downloads/myo9b.fasta";
//    reader = new FASTAReader(new File(loc1));
//
//    String ncbi_myo9b = "";
//
//    start = 0;
//    end = 137513;
//    window = 100;
//    seqLength = end - start;
//    while( ncbi_myo9b.length() < seqLength)
//      {
//      String seq = reader.readSequence(window);
//      ncbi_myo9b = ncbi_myo9b + seq;
//      if (seqLength - ncbi_myo9b.length() < window ) window = seqLength - ncbi_myo9b.length();
//      }
//
//    assertEquals(myo9b.length(), ncbi_myo9b.length());
//    assertEquals(myo9b, ncbi_myo9b);
//
//    }
//
  }
