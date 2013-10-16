/**
 * org.lcsb.lu.igcsa.watchmaker.kt.termination
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt.termination;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.watchmaker.kt.PopulationEvaluation;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.List;

public class PopulationComplexity implements TerminationCondition
  {
  static Logger log = Logger.getLogger(PopulationComplexity.class.getName());

  private double mean;
  private double SD;

  public PopulationComplexity(double mean, double SD)
    {
    this.mean = mean;
    this.SD = SD;
    }

  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    List<? extends EvaluatedCandidate<?>> population = populationData.getEvaluatedPopulation();

    PopulationEvaluation eval = new PopulationEvaluation((List<EvaluatedCandidate<KaryotypeCandidate>>) population);

    // This is going to be very dependent on the size of the population so how do I determine a reasonable SD?
    if( eval.getComplexityStats().getArithmeticMean() >= mean && eval.getComplexityStats().getStandardDeviation() >= SD )//&& eval.getBpStats().getMinimum() >= 2)
      return true;

    return false;
    }
  }
