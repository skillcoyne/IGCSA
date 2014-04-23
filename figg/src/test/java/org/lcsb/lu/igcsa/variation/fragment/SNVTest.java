package org.lcsb.lu.igcsa.variation.fragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.karyotype.database.normal.Fragment;
import org.lcsb.lu.igcsa.karyotype.database.normal.SNVProbabilityDAO;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.tables
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class SNVTest
    {
    @Autowired
    private SNVProbabilityDAO testSNVDAO;

    private SNV testSNV;
    private char[] sequence = "ACTGCTTAGCG".toCharArray();

    @Before
    public void setup() throws Exception
      {
      testSNV = new SNV();
      testSNV.setSnvFrequencies(testSNVDAO.getAll());
      }



    @Test
    public void testMutate() throws Exception
      {
//      Fragment fragment = new Fragment();
//      fragment.setVariation("SNV");
//      fragment.setCount(3);

      //testSNV.setMutationFragment(fragment);

      DNASequence oldSeq = new DNASequence(String.valueOf(sequence));
      DNASequence newSeq = testSNV.mutateSequence(oldSeq, 3);

      assertNotNull(newSeq);
      assertNotSame(newSeq.getSequence(), oldSeq.getSequence());

      // This is just testing that I kept the changes around
      Map<Location, DNASequence> lastMutations = testSNV.getLastMutations();
      assertTrue(lastMutations.size() > 0);
      char[] newSeqChars = newSeq.getSequence().toCharArray();
      for (Location loc: lastMutations.keySet())
        {
        assertNotSame(sequence[loc.getStart()], newSeqChars[loc.getStart()]);
        }
      }

    }
