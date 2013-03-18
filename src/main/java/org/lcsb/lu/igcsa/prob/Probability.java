package org.lcsb.lu.igcsa.prob;

import java.util.Comparator;

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

  // These are used by size based variations (not SNPs)
  private int minimum = 1;
  private int maximum = 1;


  /**
   * @param Name (String)
   * @param Probability (Double)
   * @param Frequency (Double)
   * @throws ProbabilityException
   */
  public Probability(String n, double prob) throws ProbabilityException
    {
    this.name = n;
    this.probability = prob;
    }

  public Probability(double prob) throws ProbabilityException
    {
    this(null, prob);
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
