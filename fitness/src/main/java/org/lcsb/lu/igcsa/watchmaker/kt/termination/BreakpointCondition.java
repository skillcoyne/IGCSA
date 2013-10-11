/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt.termination;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.watchmaker.kt.BreakpointWatcher;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.watchmaker.kt.PopulationEvaluation;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.List;
import java.util.Map;

public class BreakpointCondition implements TerminationCondition
  {
  static Logger log = Logger.getLogger(BreakpointCondition.class.getName());

  private double sizeSD = 0.0;
  private double min;

  /**
   *
   * @param sizeSD Standard deviation for the number of breakpoints each individual has, across the population.
   * @param min Minimum number of times breakpoints should show up in the population. This can be used to ensure that all breakpoints have a minimum representation.
   */
  public BreakpointCondition(double sizeSD, double min)
    {
    this.sizeSD = sizeSD;
    this.min = min;
    }

  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    List<? extends EvaluatedCandidate<?>> population = populationData.getEvaluatedPopulation();

    Map<Band, Integer> bpCounts = BreakpointWatcher.getInstance ().getBreakpointCounts();
    DataSet bpStats = new DataSet(bpCounts.size());
    for(Integer count: bpCounts.values())
      bpStats.addValue(count);


    PopulationEvaluation eval = new PopulationEvaluation((List<EvaluatedCandidate<? extends KaryotypeCandidate>>) population);
    DataSet dsSize = eval.getSizeStats();

    if ((sizeSD > 0 && dsSize.getStandardDeviation() > sizeSD + sizeSD*0.01))// || bpStats.getMinimum() >= min)
      {
      log.info("Size: " + dsSize.getStandardDeviation() + " Min:" + bpStats.getMinimum());
      return true;
      }

    log.info("Size: " + dsSize.getStandardDeviation() + " Min:" + bpStats.getMinimum());
    return false;
    }
  }
