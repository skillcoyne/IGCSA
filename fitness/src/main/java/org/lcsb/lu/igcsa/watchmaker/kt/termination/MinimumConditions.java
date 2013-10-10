package org.lcsb.lu.igcsa.watchmaker.kt.termination;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;

import org.lcsb.lu.igcsa.watchmaker.kt.PopulationEvaluation;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.List;


/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

// We're aiming for a large, diverse population rather than a single best answer
public class MinimumConditions implements TerminationCondition
  {
  static Logger log = Logger.getLogger(MinimumConditions.class.getName());

  private double sizeSD = 0.0;
  private double minFitnessSD = 0.0;

  public MinimumConditions(double sizeSD, double minFitnessSD)
    {
    this.sizeSD = sizeSD;
    this.minFitnessSD = minFitnessSD;
    }


  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    List<? extends EvaluatedCandidate<?>> population = populationData.getEvaluatedPopulation();

    PopulationEvaluation eval = new PopulationEvaluation((List<EvaluatedCandidate<? extends KaryotypeCandidate>>) population);
    DataSet dsSize = eval.getSizeStats();
    if ( (sizeSD > 0 && dsSize.getStandardDeviation() > sizeSD + sizeSD*0.01)  &&
        (minFitnessSD > 0 && populationData.getFitnessStatistics().getStandardDeviation() >= minFitnessSD) )
      return true;

    return false;
    }
  }
