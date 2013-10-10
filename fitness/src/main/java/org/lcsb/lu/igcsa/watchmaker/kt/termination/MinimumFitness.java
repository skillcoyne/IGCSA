/**
 * org.lcsb.lu.igcsa.watchmaker.kt.termination
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt.termination;

import org.apache.log4j.Logger;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.List;

public class MinimumFitness implements TerminationCondition
  {
  static Logger log = Logger.getLogger(MinimumFitness.class.getName());

  private double minFitness = 0.0;

  public MinimumFitness(double minFitness)
    {
    this.minFitness = minFitness;
    }

  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    List<? extends EvaluatedCandidate<?>> population = populationData.getEvaluatedPopulation();

    if (minFitness > 0 && populationData.getFitnessStatistics().getMinimum() >= minFitness)
      return true;

    return false;
    }
  }
