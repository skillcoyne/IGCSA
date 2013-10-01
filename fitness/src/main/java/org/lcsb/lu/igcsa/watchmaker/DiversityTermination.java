/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiversityTermination implements TerminationCondition
  {
  static Logger log = Logger.getLogger(DiversityTermination.class.getName());

  private int generationLimit = 0;
  private double sizeSD = 0.0;
  private double minFitness = 0.0;

  public DiversityTermination(int generationLimit, double sizeSD, double minFitness)
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

    DataSet dsSize = sizePopulationSizeStats(populationData);
    if ( (sizeSD > 0 && dsSize.getStandardDeviation() > sizeSD + sizeSD*0.01)  &&
        (minFitness > 0 && populationData.getFitnessStatistics().getMinimum() >= minFitness) )
      return true;


    //DataSet dsUnique = uniqueIndividualStats(populationData);

    return false;
    }


  private DataSet sizePopulationSizeStats(PopulationData<?> pd)
    {

    List<? extends EvaluatedCandidate<?>> pop = pd.getEvaluatedPopulation();
    DataSet sizeStats = new DataSet(pd.getPopulationSize());
    for (EvaluatedCandidate<?> ind: pop)
      {
      Set<Band> candidate = (Set<Band>) ind.getCandidate();
      sizeStats.addValue(candidate.size());
      }

//    log.info("--- Size stats ---");
//    outputStats(sizeStats);

    return sizeStats;
    }


  private DataSet uniqueIndividualStats(PopulationData<?> pd)
    {
    List<? extends EvaluatedCandidate<?>> population = pd.getEvaluatedPopulation();

    Map<Set<Band>, Integer> same = new HashMap<Set<Band>, Integer>();
    for (EvaluatedCandidate<?> ind : population)
      {
      if (same.containsKey(ind.getCandidate()))
        same.put((Set<Band>) ind.getCandidate(), same.get(ind.getCandidate()) + 1);
      else
        same.put((Set<Band>) ind.getCandidate(), 1);
      }

    DataSet uniqueIndStats = new DataSet(same.size());
    for (Integer val : same.values())
      uniqueIndStats.addValue(val);

//    log.info("--- Ind stats ---");
//    outputStats(uniqueIndStats);

    return uniqueIndStats;
    }



  private static void outputStats(DataSet stats)
    {
    log.info("Min: " + stats.getMinimum() + " Max: " + stats.getMaximum());
    log.info("Mean: " + stats.getArithmeticMean());
    log.info("SD: " + stats.getStandardDeviation());
    log.info("Dispersion: " + stats.getStandardDeviation()/stats.getArithmeticMean());
    }


  }
