package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.prob.Probability;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class AbstractVariation implements Variation
  {
  protected Probability probability;

  protected AbstractVariation()
    {
    super();
    }

  protected AbstractVariation(Probability p)
    {
    this.probability = p;
    }

  public void setProbability(Probability probability)
    {
    this.probability = probability;
    }

  public Probability getProbability()
    {
    return this.probability;
    }

  public abstract DNASequence mutateSequence(DNASequence sequence);
  }
