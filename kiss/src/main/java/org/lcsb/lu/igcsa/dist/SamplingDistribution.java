/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.dist;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * This class takes a set of probabilities, presuming they are normally distributed, and samples the associated objects based on that distribution.
 * Currently this is primarily useful for the distribution of unstable chromosomes.
 */
public class SamplingDistribution
  {
  static Logger log = Logger.getLogger(SamplingDistribution.class.getName());

  private TreeMap<Double, Object> objProbabilities = new TreeMap<Double, Object>();
  private Map<Object, List<Object>> duplicateKeys = new HashMap<Object, List<Object>>();

  private double mean = 0;
  private double stdDev = 0;
  private double maxValue = 0;
  private int decimalPlaces = 2;

  private NormalDistribution distribution;


  public SamplingDistribution(double[] probs, int decimals) throws Exception
    {
    decimalPlaces = decimals;
    init(probs, null);
    }

  public SamplingDistribution(double[] probs, Object[] objs, int decimals) throws Exception
    {
    decimalPlaces = decimals;
    init(probs, objs);
    }

  public Object sample()
    {
    double p = round(distribution.sample(), decimalPlaces);

    if (p >= maxValue)
      return getSampledObj(objProbabilities.lastEntry().getValue());
    else
      return getSampledObj(objProbabilities.higherEntry(p).getValue());
    }

  private Object getSampledObj(Object o)
    {
    if (duplicateKeys.containsKey(o))
      return duplicateKeys.get(o).get(new Random().nextInt(duplicateKeys.get(o).size()));
    else
      return o;
    }


  private static double round(double d, int dec)
    {
    double places = Math.pow(10, dec);
    return (double) Math.round(d * places) / places;
    }

  private void init(double[] probs, Object[] objs) throws Exception
    {
    mean = round(new Mean().evaluate(probs), decimalPlaces);
    stdDev = round(new StandardDeviation().evaluate(probs), decimalPlaces);
    maxValue = round(new Max().evaluate(probs), decimalPlaces);

    distribution = new NormalDistribution(mean, stdDev);


    for (int i = 0; i < probs.length; i++)
      {
      Object key;
      if (objs == null) key = String.valueOf(i+1);
      else key = objs[i];

      List<Object> current = new ArrayList<Object>();
      if (!duplicateKeys.containsKey(key))
        {
        current.add(key);
        duplicateKeys.put(key, current);
        }

      current = duplicateKeys.get(key);
      current.add(objProbabilities.put(round(probs[i], decimalPlaces), key));

      duplicateKeys.put(key, current);
      }
    cleanDuplicates();

    log.debug("Probs: " + objProbabilities.toString());
    log.debug("Equally likely: " + duplicateKeys.toString());

    if (objProbabilities.size() < probs.length)
      log.warn("Some object probabilities are too close to each other to distinguish. These will be sampled and then randomly selected: " + duplicateKeys.values().toString());
    }

  private void cleanDuplicates()
    {
    Iterator<Object> iter = duplicateKeys.keySet().iterator();
    while (iter.hasNext())
      {
      Object obj = iter.next();
      duplicateKeys.get(obj).remove(null);
      if (duplicateKeys.get(obj).size() == 1 && duplicateKeys.get(obj).get(0).equals(obj))
        iter.remove();
      }
    }

  }
