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
public class Probability
  {
  static Logger log = Logger.getLogger(Probability.class.getName());

  private Random generator;
  private double totalValue = 1.0; // since it's never allowed to sum to anything other than 1.0....

  protected NavigableMap<Double, Object> objProbabilities = new TreeMap<Double, Object>();

  private int decimalPlaces = 2;

  public Probability(Object[] objects, double[] probabilities) throws ProbabilityException
    {
    if (objects.length != probabilities.length)
      throw new IllegalArgumentException("Arrays must match.");
        if ( !isSumOne(probabilities) )
          throw new ProbabilityException("Sum of probabilities did not equal 1.");

    this.generator = new Random();

    double total = 0.0;
    for (int i = 0; i < probabilities.length; i++)
      {
      objProbabilities.put(probabilities[i] + total, objects[i]);
      total = total + probabilities[i];
      }

//    double sum = isSumOne(probabilities);
//    if (sum > 0)
//      addNullEntry(sum);
    }

  /**
   * Takes a map of objects and given probabilities (doubles).  The probabilities must sum to 1.
   * A cumulative probability table is generated from this.
   *
   * @param probabilities
   * @throws ProbabilityException
   */
  public Probability(Map<Object, Double> probabilities) throws ProbabilityException
    {
    this.init(probabilities);
    if ( !isSumOne(probabilities.values()) ) throw new ProbabilityException("Sum of probabilities did not equal 1.");
    this.generator = new Random();

    double total = 0.0;
    for (Map.Entry<Object, Double> entry : probabilities.entrySet())
      {
      objProbabilities.put(entry.getValue() + total, entry.getKey());
      total = entry.getValue() + total;
      }

//    double sum = isSumOne(probabilities.values());
//    if (sum > 0)
//      addNullEntry(sum);
    }

  /**
   * Takes a map of objects and given probabilities (doubles).  The probabilities must sum to 1.
   * A cumulative probability table is generated from this.
   *
   * @param probabilities
   * @param decimalPlaces
   * @throws ProbabilityException
   */
  public Probability(Map<Object, Double> probabilities, int decimalPlaces) throws ProbabilityException
    {
    this.decimalPlaces = decimalPlaces;
    this.init(probabilities);
    if (!isSumOne(probabilities.values()))
    throw new ProbabilityException("Sum of probabilities did not equal 1.");
    this.generator = new Random();

    double total = 0.0;
    for (Map.Entry<Object, Double> entry : probabilities.entrySet())
      {
      objProbabilities.put(round(entry.getValue() + total, decimalPlaces), entry.getKey());
      total = entry.getValue() + total;
      }

//    double sum = isSumOne(probabilities.values());
//    if (sum > 0)
//      addNullEntry(sum);
    }

//  private void addNullEntry(double e)
//    {
//    log.debug("Sum of probabilities did not equal 1, adding null key for remainder.");
//    objProbabilities.put(e, null);
//    }

  public NavigableMap<Double, Object> getProbabilities()
    {
    return objProbabilities;
    }

  /**
   * Randomly generates a number between 0 and 1.0.  Returns the object in the probability table with the higher probability.
   * All generated values are rounded.
   *
   * @return
   */
  public Object roll()
    {
    double p = this.generator.nextDouble();
    if (p >= totalValue || p >= objProbabilities.lastEntry().getKey())
      return objProbabilities.lastEntry().getValue();
    else
      return objProbabilities.higherEntry(p).getValue();
    }

  private boolean isSumOne(double[] doubles)
    {
    double sum = 0;

    for (int i = 0; i < doubles.length; i++)
      sum += doubles[i];
    sum = round(sum, 2);

    return (sum == 1.0)? (true): (false);
    }

  private boolean isSumOne(Collection<Double> doubles)
    {
    double sum = 0;
    Iterator<Double> ip = doubles.iterator();
    while (ip.hasNext())
      sum += ip.next();
    sum = round(sum, 2);

    return (sum == 1.0)? (true): (false);
    }

  private static double round(double d, int dec)
    {
    double places = Math.pow(10, dec);
    return (double) Math.round(d * places) / places;
    }

    private void init(Map<Object, Double> probabilities) throws ProbabilityException
      {
      if ( !isSumOne(probabilities.values()) ) throw new ProbabilityException("Sum of probabilities did not equal 1.");
      this.generator = new Random();

      double total = 0.0;
      for (Map.Entry<Object, Double> entry: probabilities.entrySet())
        {
        objProbabilities.put( round(entry.getValue()+total, this.decimalPlaces), entry.getKey() );
        total = round(total + entry.getValue(), this.decimalPlaces);
        }
      this.totalValue = round(total, this.decimalPlaces);
      }


  }
