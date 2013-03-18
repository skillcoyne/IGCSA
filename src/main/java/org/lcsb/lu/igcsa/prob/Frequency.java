package org.lcsb.lu.igcsa.prob;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Frequency
  {
  static Logger log = Logger.getLogger(Frequency.class.getName());

  private NavigableMap<Double, Object> objProbabilities = new TreeMap<Double, Object>();


  public Frequency(Map<Object, Double> probabilities) throws ProbabilityException
    {
    if ( !isSumOne(probabilities.values()) ) throw new ProbabilityException("Sum of probabilities did not equal 1.");

    for (Object obj: probabilities.keySet())
      {
      objProbabilities.put( probabilities.get(obj), obj );
      }
    }


  public Object random()
    {
    double p = new Random().nextDouble();
    log.debug( p );

    return objProbabilities.higherEntry(p);
    }


  private boolean isSumOne(Collection<Double> doubles)
    {
    double sum = 0;
    Iterator<Double> ip = doubles.iterator();
    while (ip.hasNext())
      {
      sum += ip.next();
      }
    return (sum == 1.0)? (true): (false);
    }

  }
