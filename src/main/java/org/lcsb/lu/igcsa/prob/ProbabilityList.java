package org.lcsb.lu.igcsa.prob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ProbabilityList extends ArrayList<Probability>
  {
  private double sum;

  public ProbabilityList()
    {
    super();
    }

  public ProbabilityList(Collection<? extends Probability> probabilities)
    {
    super(probabilities);
    sumProbabilities();
    }

  public boolean add(Probability probability)
    {
    boolean added = super.add(probability);
    sumProbabilities();
    return added;
    }

  public boolean addAll(int i, Collection<? extends Probability> probabilities)
    {
    boolean added = super.addAll(i, probabilities);
    sumProbabilities();
    return added;
    }

  public boolean addAll(Collection<? extends Probability> probabilities)
    {
    boolean added = super.addAll(probabilities);
    sumProbabilities();
    return added;
    }

  private void sumProbabilities() throws ProbabilityError
    {
    sum = 0;
    Iterator<Probability> ip = this.iterator();
    while (ip.hasNext())
      {
      Probability p = ip.next();
      sum += p.getProbability();
      }
    if (sum != 1.0) throw new ProbabilityError("Probabilities must sum to 1 (" + sum + ")");
    }
  }


