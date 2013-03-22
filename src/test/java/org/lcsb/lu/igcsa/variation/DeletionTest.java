package org.lcsb.lu.igcsa.variation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class DeletionTest
  {
  static Logger log = Logger.getLogger(DeletionTest.class.getName());

  @Autowired
  private Properties testNormalProperties;

  private Deletion deletion;

  private char[] sequence = "ACTGCTTAGCGTATAG".toCharArray();


  @Before
  public void setUp() throws Exception
    {
    deletion = new Deletion();
    Fragment fragment = new Fragment();
    fragment.setDeletion(3);

    deletion.setMutationFragment(fragment);
    }

  @Test
  public void testMutateSequence() throws Exception
    {
    DNASequence oldSeq = new DNASequence(String.valueOf(sequence));
    DNASequence newSeq = deletion.mutateSequence(oldSeq);

    assertNotSame(oldSeq, newSeq);
    assertTrue(newSeq.getLength() < oldSeq.getLength());
    }
  }
