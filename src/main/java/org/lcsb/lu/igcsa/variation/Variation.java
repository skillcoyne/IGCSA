package org.lcsb.lu.igcsa.variation;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Fragment;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;

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

    protected Random siteSelector = new Random();

    public void setMutationFragment(Fragment fragment)
      {
      this.fragment = fragment;
      }

    public abstract DNASequence mutateSequence(String sequence);

    public DNASequence mutateSequence(DNASequence sequence)
      {
      return mutateSequence(sequence.getSequence());
      }
    }
