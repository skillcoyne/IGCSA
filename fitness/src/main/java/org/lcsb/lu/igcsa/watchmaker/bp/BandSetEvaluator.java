/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.bp;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.prob.Probability;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BandSetEvaluator implements FitnessEvaluator<Set<Band>>
  {
  static Logger log = Logger.getLogger(BandSetEvaluator.class.getName());

  private Map<Band, Double> probabilities = new HashMap<Band, Double>();
  private Probability bandCountProbability;
  private double sumProb;

  public BandSetEvaluator(Probability bandProbability, Probability bandCountProbability)
    {

    for (Map.Entry<Object, Double> entry : bandProbability.getRawProbabilities().entrySet())
      {
      probabilities.put((Band) entry.getKey(), entry.getValue()*100);
      sumProb += entry.getValue();
      }

//    for (Map.Entry<Double, Object> entry : bandProbability.getProbabilities().entrySet())
//      {
//      probabilities.put((Band) entry.getValue(), entry.getKey());
//      sumProb += entry.getKey();
//      }

    this.bandCountProbability = bandCountProbability;

    log.info(sumProb);
    }

  @Override
  public double getFitness(Set<Band> bands, List<? extends Set<Band>> sets)
    {
    double score = 0.0;
    for (Band b : bands)
      score += probabilities.get(b);

    // while technically a genome with no mutations is perfectly fit that is not what I'm looking for
    if (score == 0)
      score = sumProb;

    //score += getWeight(bands.size());

    return score;
    }

  @Override
  public boolean isNatural()
    {
    return false;
    }

  private double getWeight(int numberBands)
    {
    Set<Object> ranges = bandCountProbability.getRawProbabilities().keySet();

    double weight = 1.0;
    for (Object r: ranges)
      {
      IntRange rg = (IntRange) r;
      if (rg.containsInteger(numberBands))
        weight = bandCountProbability.getRawProbabilities().get(rg);
      }

    return Math.abs(Math.log(weight))/100;
    }


  }
