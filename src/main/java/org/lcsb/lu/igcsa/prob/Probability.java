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
  private double frequency;

  // These are used by size based variations (not SNPs)
  private int minimum = 1;
  private int maximum = 1;


  public Probability(double prob, double freq) throws ProbabilityException
    {
    this(null, prob, freq);
    }

  /**
   *
   * @param Name (String)
   * @param Probability (Double)
   * @param Frequency (Double)
   * @throws ProbabilityException
   */
  public Probability(String n, double prob, double freq) throws ProbabilityException
    {
    this.name = n;
    this.probability = prob;
    this.frequency = freq;
    if (frequency <= 0)
      throw new ProbabilityException("Frequency must be greater than 0.");
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

  public double getFrequency()
    {
    return this.frequency;
    }

  }
