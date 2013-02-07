package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.Sequence;
import org.lcsb.lu.igcsa.prob.Probability;

/**
 * org.lcsb.lu.igcsa.variation
 * User: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open source License Apache 2
 */
public class Translocation extends Variation
  {

  public Translocation(Location l, Probability p, Sequence seq)
    {
    super(l, p, seq);
    }

  public Sequence mutateSequence()
    {
    return null;
    }
  }
