package org.lcsb.lu.igcsa.variation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.SNP;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Probability;
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
public class SNPTest
    {
    @Autowired
    private Properties testProperties;

    private SNP snp;
    private Map<Character, ProbabilityList> probabilityList;
    private String sequence = "ACTGCTTAGCG";

    @Before
    public void setUp() throws Exception
      {
//      GenomeProperties props = GenomeProperties.readPropertiesFile("test.properties", GenomeProperties.GenomeType.NORMAL);
//      GenomeProperties snpPropertySet = props.getVariationProperty("snp");
//
//      double frequency = 1.0; // ensures that something will mutate
//
//      probabilityList = new HashMap<Character, ProbabilityList>();
//      for (char base : "ACTG".toCharArray())
//        {
//        String baseFrom = Character.toString(base);
//        GenomeProperties baseProps = snpPropertySet.getPropertySet("base").getPropertySet(baseFrom);
//
//        ProbabilityList pList = new ProbabilityList();
//        for (String baseTo : baseProps.stringPropertyNames())
//          {
//          pList.add(new Probability(baseTo, Double.valueOf(baseProps.getProperty(baseTo)), frequency));
//          }
//        assertTrue(pList.isSumOne());
//        probabilityList.put(base, pList);
//        }
//      assertEquals(probabilityList.size(), 4);
//
//      this.snp = new SNP();
//      assertNotNull("SNP object wasn't created", snp);
      }


    @Test
    public void testMutate() throws Exception
      {
//      String mutatedSeq = "";
//      for (char nucleotide: sequence.toCharArray())
//        {
//        String currentNucleotide = Character.toString(nucleotide);
//        snp.setProbabilityList(probabilityList.get(nucleotide));
//        mutatedSeq = mutatedSeq + snp.mutateSequence(currentNucleotide).getSequence();
//        }
//      assertEquals(mutatedSeq.length(), sequence.length());
//      assertFalse("Mutated sequence should have at least 1bp change: " + sequence + "-->" + mutatedSeq, mutatedSeq.equals(sequence));
      }

    }
