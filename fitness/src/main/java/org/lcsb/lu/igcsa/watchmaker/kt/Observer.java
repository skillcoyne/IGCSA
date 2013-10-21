/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.utils.GenerationStatistics;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

import java.util.ArrayList;
import java.util.List;

public class Observer implements EvolutionObserver<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(Observer.class.getName());

  private List<Double> minFitness = new ArrayList<Double>();
  private List<Double> sizeStdDev = new ArrayList<Double>();
  private List<Double> meanFitness = new ArrayList<Double>();
  private List<Double> maxFitness = new ArrayList<Double>();

  @Override
  public void populationUpdate(PopulationData populationData)
    {
    log.info("*** Generation " + populationData.getGenerationNumber() + "  Size: " + populationData.getPopulationSize() + " ***");

    PopulationEvaluation eval = new PopulationEvaluation(populationData.getEvaluatedPopulation());
    eval.outputCurrentStats();
    GenerationStatistics.getTracker().track(eval);

    sizeStdDev.add(eval.getSizeStats().getStandardDeviation());
    minFitness.add(eval.getFitnessStats().getMinimum());
    meanFitness.add(eval.getFitnessStats().getArithmeticMean());
    maxFitness.add(eval.getFitnessStats().getMaximum());
    }

  public List<Double> getMeanFitness()
    {
    return meanFitness;
    }

  public List<Double> getSizeStdDev()
    {
    return sizeStdDev;
    }

  public List<Double> getMinFitness()
    {
    return minFitness;
    }

  public List<Double> getMaxFitness()
    {
    return maxFitness;
    }
  }
