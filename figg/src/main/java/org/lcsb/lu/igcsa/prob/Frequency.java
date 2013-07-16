package org.lcsb.lu.igcsa.prob;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Iterator;


/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Frequency
  {
  static Logger log = Logger.getLogger(Frequency.class.getName());

  private Random generator;
  private double totalValue;
  private NavigableMap<Double, Object> objProbabilities = new TreeMap<Double, Object>();
  private double rounder = 100.0;

  /**
   * Takes a map of objects and given probabilities (doubles).  The probabilities must sum to 1.
   * A cumulative probability table is generated from this.
   * @param probabilities
   * @throws ProbabilityException
   */
  public Frequency(Map<Object, Double> probabilities) throws ProbabilityException
    {
    this.init(probabilities);
    }

  /**
   * Takes a map of objects and given probabilities (doubles).  The probabilities must sum to 1.
   * A cumulative probability table is generated from this.
   * @param probabilities
   * @throws ProbabilityException
   */
  public Frequency(Map<Object, Double> probabilities, double rounder) throws ProbabilityException
    {
    this.rounder = rounder;
    this.init(probabilities);
    }


  public NavigableMap<Double, Object> getProbabilities()
    {
    return objProbabilities;
    }

  /**
   * Randomly generates a number between 0 and 1.0.  Returns the object in the probability table with the higher probability.
   * All generated values are rounded to 2 digits.
   * @return
   */
  public Object roll()
    {
    double p = this.generator.nextDouble();

    if (p >= totalValue) return objProbabilities.lastEntry().getValue();
    else return objProbabilities.higherEntry(p).getValue();
    }

  private boolean isSumOne(Collection<Double> doubles)
    {
    double sum = 0;
    Iterator<Double> ip = doubles.iterator();
    while (ip.hasNext()) sum += ip.next();
    sum = round(sum);

    return (sum == 1.0)? (true): (false);
    }

  private double round(double p)
    {
    return (double) Math.round(p*rounder)/rounder;
    }

  private void init(Map<Object, Double> probabilities) throws ProbabilityException
    {
    if ( !isSumOne(probabilities.values()) ) throw new ProbabilityException("Sum of probabilities did not equal 1.");
    this.generator = new Random();

    double total = 0.0;
    for (Map.Entry<Object, Double> entry: probabilities.entrySet())
      {
      objProbabilities.put( round(entry.getValue()+total), entry.getKey() );
      total = round(total + entry.getValue());
      }
    this.totalValue = round(total);
    }


  }
