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
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Frequency;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class TandemRepeatTest
  {
  static Logger log = Logger.getLogger(TandemRepeatTest.class.getName());
  private char[] sequence = "ACTGCTTAGCGTATAG".toCharArray();

  private Variation tandemRpt;


  @Before
  public void setUp() throws Exception
    {
    tandemRpt = new TandemRepeat();
    Fragment fragment = new Fragment();
    fragment.setCount(3);

    tandemRpt.setMutationFragment(fragment);

    Map<Object, Double> probs = new TreeMap<Object, Double>();
    probs.put(3, 0.9897);
    probs.put(7, 0.0086);
    probs.put(10, 0.0017);
    Frequency f = new Frequency(probs);

    tandemRpt.setSizeVariation(f);
    }

  @Test
  public void testMutateSequence() throws Exception
    {
    DNASequence oldSeq = new DNASequence(String.valueOf(sequence));
    DNASequence newSeq = tandemRpt.mutateSequence(oldSeq);

    assertNotSame(oldSeq, newSeq);
    assertTrue(newSeq.getLength() > oldSeq.getLength());
    }
  }
