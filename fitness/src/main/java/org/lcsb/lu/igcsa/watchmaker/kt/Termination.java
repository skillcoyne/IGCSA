package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.watchmaker.bp.DEPopulationEvaluation;
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
public class Termination implements TerminationCondition
  {
  static Logger log = Logger.getLogger(Termination.class.getName());

  private int generationLimit = 0;
  private double sizeSD = 0.0;
  private double minFitness = 0.0;

  public Termination(int generationLimit, double sizeSD, double minFitness)
    {
    log.info("initialize");
    this.generationLimit = generationLimit;
    this.sizeSD = sizeSD;
    this.minFitness = minFitness;
    }


  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    if (generationLimit > 0 && populationData.getGenerationNumber() >= generationLimit)
      return true;

    List<? extends EvaluatedCandidate<?>> population = populationData.getEvaluatedPopulation();
    PopulationEvaluation eval = new PopulationEvaluation(population);

    DataSet dsSize = sizePopulationSizeStats(populationData);
    if ( (sizeSD > 0 && dsSize.getStandardDeviation() > sizeSD + sizeSD*0.01)  &&
        (minFitness > 0 && populationData.getFitnessStatistics().getMinimum() >= minFitness) )
      return true;


    //DataSet dsUnique = uniqueIndividualStats(populationData);

    return false;

    }
  }
