package org.lcsb.lu.igcsa.variation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.variation.SNP;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.utils.GenomeProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class SNPTest
    {
    private SNP snp;
    private DNASequence sequence;


    @Before
    public void setUp() throws Exception
      {
      GenomeProperties props = GenomeProperties.readPropertiesFile("test.properties", GenomeProperties.GenomeType.NORMAL);
      Properties baseProps = props.getPropertySet("base");

      Collection<Probability> probabilities = new ArrayList<Probability>();
      for(String bp: baseProps.stringPropertyNames())
        {
        probabilities.add( new Probability(bp, Double.valueOf(baseProps.getProperty(bp)) ) );
        }


      sequence = new DNASequence("actgcttagcgc");
      this.snp = new SNP( new Probability(0.7/sequence.getLength()) );
      //snp = new SNP( new Location(1,100), new Probability(0.7/sequence.getLength()), sequence, probabilities.toArray( new Probability[probabilities.size()] ));
      assertNotNull("SNP object wasn't created", snp);
      }


    @Test
    public void testMutate() throws Exception
      {
      assertNotSame("Original sequence should not match mutated sequence (maybe)", snp.mutateSequence(sequence), sequence);
      }

    }
