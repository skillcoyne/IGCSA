package org.lcsb.lu.igcsa.variation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class SNVTest
    {
    @Autowired
    private Properties testNormalProperties;

    @Autowired
    private SNV SNV;

    private char[] sequence = "ACTGCTTAGCG".toCharArray();

    @Test
    public void testMutate() throws Exception
      {
      Fragment fragment = new Fragment();
      fragment.setSNV(3);

      SNV.setMutationFragment(fragment);

      DNASequence oldSeq = new DNASequence(String.valueOf(sequence));
      DNASequence newSeq = SNV.mutateSequence(oldSeq);

      assertNotNull(newSeq);
      assertNotSame(newSeq.getSequence(), oldSeq.getSequence());

      // This is just testing that I kept the changes around
      Map<Location, DNASequence> lastMutations = SNV.getLastMutations();
      char[] newSeqChars = newSeq.getSequence().toCharArray();
      for (Location loc: lastMutations.keySet())
        {
        assertNotSame(sequence[loc.getStart()], newSeqChars[loc.getStart()]);
        }
      }

    }
