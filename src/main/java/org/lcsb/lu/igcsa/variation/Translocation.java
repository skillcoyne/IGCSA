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
public class Translocation extends AbstractVariation
  {
  public Translocation()
    {
    }

  public Translocation(Probability p)
    {
    super(p);
    }

  //TODO  this may not work for translocations...
  public DNASequence mutateSequence(DNASequence sequence)
    {
    return null;
    }
  }
