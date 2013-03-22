package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

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
    protected Fragment fragment;

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

    public void setMutationFragment(Fragment fragment)
      {
      this.fragment = fragment;
      }

    public Map<Location, DNASequence> getLastMutations()
      {
      return this.lastMutations;
      }


    public abstract DNASequence mutateSequence(String sequence);

    public DNASequence mutateSequence(DNASequence sequence)
      {
      return mutateSequence(sequence.getSequence());
      }
    }
