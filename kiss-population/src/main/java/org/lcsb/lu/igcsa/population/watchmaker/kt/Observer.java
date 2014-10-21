/**
 * org.lcsb.lu.igcsa.population.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.population.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.population.watchmaker.kt.statistics.PopulationStatistic;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;


public class Observer implements EvolutionObserver<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(Observer.class.getName());

  public static boolean QUIET = false;

  private static int generations = 0;

  private static PopulationData lastData;

  private PopulationStatistic[] statistics;

  public Observer(PopulationStatistic... statistics)
    {
    this.statistics = statistics;
    }

  @Override
  public void populationUpdate(PopulationData populationData)
    {
    StringBuffer buffer = new StringBuffer("*** Generation " + populationData.getGenerationNumber());
    buffer.append("  Size: " + populationData.getPopulationSize() + " ***\n");
    for (PopulationStatistic statistic: statistics)
      {
      DataSet stats = statistic.getStatistics(populationData.getEvaluatedPopulation());
      buffer.append( "---" + statistic.getClass().getSimpleName() + "---\n");
      buffer.append("\tMin: " + stats.getMinimum());
      buffer.append("\tMax: " + stats.getMaximum());
      buffer.append("\tMean: " + stats.getArithmeticMean());
      buffer.append("\tSD: " + stats.getStandardDeviation() + "\n");
      }

    if (!QUIET)
      log.info("\n" + buffer.toString());
    else
      log.debug("\n" + buffer.toString());

    generations = populationData.getGenerationNumber();
    lastData = populationData;
    }

  public int generationCount()
    {
    return generations;
    }

  public void finalUpdate()
    {
    QUIET = false;
    this.populationUpdate(lastData);
    }

  }
