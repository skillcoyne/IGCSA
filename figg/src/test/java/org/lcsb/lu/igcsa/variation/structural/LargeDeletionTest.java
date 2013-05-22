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
public class LargeDeletionTest
  {
  static Logger log = Logger.getLogger(LargeDeletionTest.class.getName());

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

  private StructuralVariation cnl;

  @Before
  public void setUp() throws Exception
    {
    cnl = new LargeDeletion();
    cnl.setVariationName("copy_number_loss");
    //cnl.setLocation(497, 532);
    assertNotNull(cnl);
    }

  @Test
  public void testMutateSequence() throws Exception
    {
    DNASequence newSequence = cnl.mutateSequence(fastaSeq);
    assertNotSame(newSequence.getSequence(), fastaSeq);
    assertEquals(newSequence.getSequence().indexOf('-'), -1);
    }

  }
