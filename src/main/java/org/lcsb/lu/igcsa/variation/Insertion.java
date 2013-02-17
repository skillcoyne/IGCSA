package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Probability;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Insertion extends AbstractVariation
  {

  public Insertion()
    {
    }

  public Insertion(Probability p)
    {
    super(p);
    }

  public DNASequence mutateSequence(DNASequence sequence)
    {
    return null;
    }


  }
