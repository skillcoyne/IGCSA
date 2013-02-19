package org.lcsb.lu.igcsa.utils;

import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.SNP;
import org.lcsb.lu.igcsa.variation.Variation;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenomeUtils
  {

  /*
 Deletions/insertions/inversions all have a size related probability.
  */
  public static Genome setupSizeVariation(GenomeProperties varPropertySet, Genome genome, Variation variation) throws ProbabilityException
    {
    ProbabilityList pList = new ProbabilityList();

    GenomeProperties sizeProps = varPropertySet.getPropertySet("size");
    for (String size : sizeProps.stringPropertyNames())
      {
      Probability prob = new Probability(size, Double.valueOf(sizeProps.getProperty(size)), Double.valueOf(varPropertySet.getProperty("freq")));
      //      prob.setMinimumBases(Integer.valueOf(varPropertySet.getProperty("min")));
      //      prob.setMaximumBases(Integer.valueOf(varPropertySet.getProperty("max")));
      pList.add(prob);
      }
    if (!pList.isSumOne()) throw new ProbabilityException(variation.getClass().toString() + " size probabilities do not sum to 1");

    genome.addVariationType(variation, pList);
    return genome;
    }

  /*
  For SNPs there is a probability for each nucleotide being any one of the others
  ProbabilityList per base, e.g. A has a list that encompasses A->G, A->C, A->T, A->A
   */
  public static Genome setupSNPs(GenomeProperties snpPropertySet, Genome genome) throws ProbabilityException
    {
    double frequency = Double.valueOf(snpPropertySet.getProperty("freq"));

    for (char base : "ACTG".toCharArray())
      {
      String baseFrom = Character.toString(base);
      GenomeProperties baseProps = snpPropertySet.getPropertySet("base").getPropertySet(baseFrom);

      ProbabilityList pList = new ProbabilityList();
      for (String baseTo : baseProps.stringPropertyNames())
        {
        pList.add(new Probability(baseTo, Double.valueOf(baseProps.getProperty(baseTo)), frequency));
        }
      if (!pList.isSumOne()) throw new ProbabilityException("SNP probabilities for " + baseFrom + " do not sum to 1");

      genome.addVariationType(new SNP(new DNASequence(baseFrom)), pList);
      }
    return genome;
    }

  }
