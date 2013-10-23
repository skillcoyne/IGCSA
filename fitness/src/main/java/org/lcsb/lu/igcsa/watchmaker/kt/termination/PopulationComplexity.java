/**
 * org.lcsb.lu.igcsa.watchmaker.kt.termination
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt.termination;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.CandidateGraph;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.watchmaker.kt.PopulationEvaluation;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.List;

public class PopulationComplexity implements TerminationCondition
  {
  static Logger log = Logger.getLogger(PopulationComplexity.class.getName());

  private double mean = 0.0;
  private double SD = 0.0;

  private DoubleRange CV;
  private int genTest = 0;

  public PopulationComplexity(double mean, double SD, DoubleRange CV, int testAfterGen)
    {
    this.mean = mean;
    this.SD = SD;
    this.CV = CV;
    this.genTest = testAfterGen;
    }


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

    double currentMean = eval.getComplexityStats().getArithmeticMean();
    double currentSD = eval.getComplexityStats().getStandardDeviation();

    // This is going to be very dependent on the size of the population so how do I determine a reasonable SD?
    if (populationData.getGenerationNumber() >= genTest && genTest > 0)
      {
      if ( populationData.getGenerationNumber() > this.genTest &&
           (this.CV.containsDouble(currentSD/currentMean ) && currentMean >= this.mean && currentSD >= this.SD)
          )
        return true;
      }
    else
      if( currentMean >= mean && currentSD  >= SD ) return true;


      return false;
    }
  }
