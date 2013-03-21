package org.lcsb.lu.igcsa.variation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.database.Fragment;
import org.lcsb.lu.igcsa.genome.DNASequence;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class InsertionTest
  {
  static Logger log = Logger.getLogger(InsertionTest.class.getName());

  private char[] sequence = "ACTGCTTAGCGTATAG".toCharArray();

  private Variation insertion;

  @Before
  public void setUp() throws Exception
    {
    insertion = new Insertion();
    Fragment fragment = new Fragment();
    fragment.setInsertion(2);

    insertion.setMutationFragment(fragment);
    }

  @Test
  public void testMutateSequence() throws Exception
    {
    DNASequence oldSeq = new DNASequence(String.valueOf(sequence));
    DNASequence newSeq = insertion.mutateSequence(oldSeq);

    assertNotSame(oldSeq, newSeq);
    assertTrue(newSeq.getLength() > oldSeq.getLength());

    }
  }
