package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Deletion extends AbstractVariation
  {

  public Deletion()
    {
    }


  public Deletion(Probability p) throws ProbabilityException
    {
    super(p);
    }

  public DNASequence mutateSequence(DNASequence sequence)
    {
    return null;
    }


  }
