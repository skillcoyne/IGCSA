/**
 * org.lcsb.lu.igcsa.variation.fragment
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.lcsb.lu.igcsa.variation.fragment;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Probability;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;


public class SubstitutionTest
  {
  static Logger log = Logger.getLogger(SubstitutionTest.class.getName());

  private char[] sequence = "ACTGCTTAGCGTATAG".toCharArray();

  private Variation substitution;


  @Before
  public void setUp() throws Exception
    {
    substitution = new Substitution();
    Fragment fragment = new Fragment();
    fragment.setCount(3);

    substitution.setMutationFragment(fragment);

    Map<Object, Double> probs = new TreeMap<Object, Double>();
    probs.put(1, 0.9897);
    probs.put(5, 0.0086);
    probs.put(10, 0.0017);
    Probability f = new Probability(probs);

    substitution.setSizeVariation(f);
    }


  @Test
  public void testMutateSequence() throws Exception
    {
    DNASequence oldSeq = new DNASequence(String.valueOf(sequence));
    DNASequence newSeq = substitution.mutateSequence(oldSeq);

    assertNotSame(oldSeq, newSeq);
    assertEquals(newSeq.getLength(), oldSeq.getLength());
    }
  }
