package org.lcsb.lu.igcsa.genome;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.fasta.FASTAReader;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ChromosomeTest extends TestCase
  {
  private Chromosome chromosome;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    File file = new File(testUrl.toURI());

    chromosome = new Chromosome("test", file);
    }

  @Test
  public void testSequenceByWindow() throws Exception
    {
    int window = 100;
    String seq = chromosome.readSequence(window).getSequence();
    assertNotNull(seq);
    assertEquals(seq.length(), window);
    assertNotSame(chromosome.readSequence(window), seq);
    }

  @Test
  public void testGetAllSequenceByWindow() throws Exception
    {
    int window = 50;
    String currentSeq;
    String all = "";
    while(true)
      {
      currentSeq = chromosome.readSequence(window).getSequence();
      all = all + currentSeq;
      if (currentSeq.length() < window) break;
      }
    assertEquals(all.length(), 644);
    }

  @Test
  public void testGetAllSequence() throws Exception
    {
    DNASequence sequence = chromosome.retrieveFullSequence();
    assertEquals(sequence.getLength(), 644);
    }

//  @Test
//  public void testAlterSequence() throws Exception
//    {
//    Location loc1 = new Location(112, 264);
//    DNASequence sequence = chromosome.getSequence(loc1);
//    assertEquals(sequence.getLength(), 264-112);
//
//    char[] s = sequence.getSequence().toCharArray();
//    s[10] = '-'; s[75] = '-';
//    DNASequence seq1 = new DNASequence(String.valueOf(s));
//
//    assertNotSame(sequence, seq1);
//    chromosome.alterSequence(loc1, seq1);
//
//
//    for (int i=12; i<20; i++) s[i] = '-';
//
//    Location loc2 = new Location(300, 452);
//    DNASequence seq2 = new DNASequence(String.valueOf(s));
//    chromosome.alterSequence(loc2, seq2);
//
//    chromosome.retrieveFullSequence();
//
//    }


  }
