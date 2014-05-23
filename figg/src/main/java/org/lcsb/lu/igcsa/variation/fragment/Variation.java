package org.lcsb.lu.igcsa.variation.fragment;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.prob.Probability;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class Variation
    {
    static Logger log = Logger.getLogger(Variation.class.getName());

//    protected Fragment fragment;
    protected Probability sizeVariation;

    protected LinkedHashMap<Location, DNASequence> lastMutations;
    protected Random siteSelector = new Random();
    protected String variationName;

    public void setVariationName(String name)
      {
      this.variationName = name;
      }

    public String getVariationName()
      {
      return this.variationName;
      }

//    public void setMutationFragment(Fragment fragment)
//      {
//      this.fragment = fragment;
//      }

    public void setSizeVariation(Probability probability)
      {
      this.sizeVariation = probability;
      }

    public int getRandomVarLength()
      {
      int max = (Integer) sizeVariation.roll();
      int min = 0;

      Map.Entry<Double, Object> lastRoll = sizeVariation.getLastRoll();
      //sizeVariation.getProbabilities().higherEntry(p.getLastRoll().getKey()).getValue()

      if (sizeVariation.getProbabilities().higherEntry(lastRoll.getKey()) != null)
        min = (Integer) sizeVariation.getProbabilities().higherEntry(lastRoll.getKey()).getValue();

      //if (sizeVariation.getRawProbabilities().lowerKey(max) != null)
        //min = (Integer) sizeVariation.getRawProbabilities().lowerKey(max);

      return new RandomRange(min, max).nextInt();
      }

    public Map<Location, DNASequence> getLastMutations()
      {
      return this.lastMutations;
      }

    public abstract DNASequence mutateSequence(String sequence, int n);

    public DNASequence mutateSequence(DNASequence sequence, int n)
      {
      return mutateSequence(sequence.getSequence(), n);
      }

    @Override
    public String toString()
      {
      return this.getClass().getSimpleName() ;
      }
    }
