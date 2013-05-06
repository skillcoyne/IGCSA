package org.lcsb.lu.igcsa.variation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Frequency;
import org.lcsb.lu.igcsa.variation.fragment.Insertion;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.util.Map;
import java.util.TreeMap;

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

  private char[] sequence = "ACTGCTTAGCGTATAG".toCharArray();

  private Variation insertion;

  @Before
  public void setUp() throws Exception
    {
    insertion = new Insertion();
    Fragment fragment = new Fragment();
    fragment.setCount(2);

    insertion.setMutationFragment(fragment);


    Map<Object, Double> probs = new TreeMap<Object, Double>();
    probs.put(1, 0.9897);
    probs.put(5, 0.0086);
    probs.put(10, 0.0017);
    Frequency f = new Frequency(probs);

    insertion.setSizeVariation(f);
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
