package org.lcsb.lu.igcsa.variation.structural;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.genome.DNASequence;

import static org.junit.Assert.*;
/**
 * org.lcsb.lu.igcsa.variation.structural
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class CopyNumberLossTest
  {
  static Logger log = Logger.getLogger(CopyNumberLossTest.class.getName());

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

  private CopyNumberLoss cnl;

  @Before
  public void setUp() throws Exception
    {
    cnl = new CopyNumberLoss();
    cnl.setLocation(497, 532);
    cnl.addFragment(new DNASequence(fastaSeq));
    assertNotNull(cnl);
    }

  @Test
  public void testMutateSequence() throws Exception
    {
    DNASequence newSequence = cnl.mutateSequence();
    assertNotSame(newSequence.getSequence(), fastaSeq);
    assertEquals(newSequence.getSequence().indexOf('-'), -1);
    }

  }
