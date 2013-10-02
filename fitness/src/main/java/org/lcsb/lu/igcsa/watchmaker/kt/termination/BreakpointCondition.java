/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt.termination;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.watchmaker.kt.PopulationEvaluation;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.List;

public class BreakpointCondition implements TerminationCondition
  {
  static Logger log = Logger.getLogger(BreakpointCondition.class.getName());

  private double sizeSD = 0.0;

  public BreakpointCondition(double sizeSD)
    {
    this.sizeSD = sizeSD;
    }

  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    List<? extends EvaluatedCandidate<?>> population = populationData.getEvaluatedPopulation();

    PopulationEvaluation eval = new PopulationEvaluation((List<EvaluatedCandidate<? extends KaryotypeCandidate>>) population);
    DataSet dsSize = eval.getSizeStats();
    if (sizeSD > 0 && dsSize.getStandardDeviation() > sizeSD + sizeSD*0.01)
      return true;

    return false;
    }
  }
