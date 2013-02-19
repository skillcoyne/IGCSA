package org.lcsb.lu.igcsa.prob;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ProbabilityList extends ArrayList<Probability> implements Comparable<ProbabilityList>
  {
  private double sum;
  private double frequency = 0;

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
    try
      { testFrequency(); }
    catch (ProbabilityException pe)
      { pe.printStackTrace(); }
    return added;
    }

  public boolean addAll(int i, Collection<? extends Probability> probabilities)
    {
    boolean added = super.addAll(i, probabilities);
    sumProbabilities();
    try
      { testFrequency(); }
    catch (ProbabilityException pe)
      { pe.printStackTrace(); }
    return added;
    }

  public boolean addAll(Collection<? extends Probability> probabilities)
    {
    boolean added = super.addAll(probabilities);
    sumProbabilities();
    try
      { testFrequency(); }
    catch (ProbabilityException pe)
      { pe.printStackTrace(); }
    return added;
    }

  public double getFrequency() throws ProbabilityException
    {
    return this.frequency;
    }


  @Override
  public Probability[] toArray()
    {
    return super.toArray(new Probability[this.size()]);    //To change body of overridden methods use File | Settings | File Templates.
    }

  public boolean isSumOne()
    {
    return (sum != 1.0) ? (false) : (true);
    }

  private void testFrequency() throws ProbabilityException
    {
    for (Probability p : this.toArray())
      {
      if (this.frequency == 0) this.frequency = p.getFrequency();
      if (this.frequency != p.getFrequency()) throw new ProbabilityException("Frequency of Probabilites must all be the same.");
      }
    }

  private void sumProbabilities() //throws ProbabilityException
    {
    sum = 0;
    Iterator<Probability> ip = this.iterator();
    while (ip.hasNext())
      {
      Probability p = ip.next();
      sum += p.getProbability();
      }
    }


  public Probability[] sort()
    {
    Collections.sort(this, new Comparator<Probability>()
      {
      public int compare(Probability p1, Probability p2)
        {
        int compare = (p1.getProbability() >= p2.getProbability())? 0: 1;
        return compare;
        }
      }
    );
    return this.toArray();
    }


  public int compareTo(ProbabilityList pl)
    {
    int compare = (this.frequency > pl.frequency)? 0: 1;
    return compare;
    }
  }


