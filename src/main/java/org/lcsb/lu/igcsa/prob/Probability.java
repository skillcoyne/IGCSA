package org.lcsb.lu.igcsa.prob;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

public class Probability
  {
  private String name; // mostly useful for SNPs
  private double probability;
  private double frequency; // not sure about this one

  public Probability(String n, double prob)
    {
    this.name = n; this.probability = prob;
    }

  public Probability(double prob)
    {
    this.probability = prob;
    }

  public String getName()
    {
    return this.name;
    }

  public double getProbability()
    {
    return this.probability;
    }

  }
