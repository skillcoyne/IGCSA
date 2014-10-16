/**
 * org.lcsb.lu.igcsa.population.watchmaker.kt.termination
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa.population.watchmaker.kt.termination;

import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.population.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.population.watchmaker.kt.statistics.*;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PopulationConditions implements TerminationCondition
  {
  static Logger log = Logger.getLogger(PopulationConditions.class.getName());

  private PopulationStatistic[] statistics;

  private List<PopulationStatistic> conditions = new ArrayList<PopulationStatistic>();

  public PopulationConditions(PopulationStatistic... statistics)
    {
    this.statistics = statistics;
    }

  public List<PopulationStatistic> getConditions()
    {
    return conditions;
    }

  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    boolean terminate = false;

    List<? extends EvaluatedCandidate<?>> candidates = populationData.getEvaluatedPopulation();


    for (PopulationStatistic stat : statistics)
      {
      DataSet dataSet = stat.getStatistics((List<EvaluatedCandidate<KaryotypeCandidate>>) candidates);

      boolean statTest = true;
      for (Map.Entry<Statistic, Double> entry : stat.getExpectedStatistics().entrySet())
        {
        switch (entry.getKey())
          {
          case MIN:
            terminate = entry.getValue() <= dataSet.getMinimum();
            break;
          case MAX:
            terminate = entry.getValue() >= dataSet.getMaximum();
            break;
          case MEAN:  // within 1 SD
            terminate = (entry.getValue() <= (dataSet.getArithmeticMean() + dataSet.getStandardDeviation()) &&
                entry.getValue() >= (dataSet.getArithmeticMean() - dataSet.getStandardDeviation()));
            break;
          case SDEV:
            terminate = entry.getValue() >= dataSet.getStandardDeviation();
            break;
          }

        // all should be true so
        if (!terminate)
          break;
        }

      if (terminate)
        conditions.add(stat);
      }

    return terminate;
    }
  }
