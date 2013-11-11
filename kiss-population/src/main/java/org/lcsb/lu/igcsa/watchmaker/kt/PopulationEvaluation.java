package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.watchmaker.kt.statistics.BreakpointRepresentation;
import org.lcsb.lu.igcsa.watchmaker.kt.statistics.CandidateBreakpoints;
import org.lcsb.lu.igcsa.watchmaker.kt.statistics.CandidateEvaluation;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;

import java.util.*;


/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class PopulationEvaluation
  {
  static Logger log = Logger.getLogger(PopulationEvaluation.class.getName());

  private List<EvaluatedCandidate<KaryotypeCandidate>> population;

  private DataSet fitnessStats;
  private DataSet sizeStats;
  private DataSet bpStats;

  public PopulationEvaluation(List<EvaluatedCandidate<KaryotypeCandidate>> candidateList)
    {
    this.population = candidateList;

    populationFitnessStats();
    sizePopulationSizeStats();
    //breakpointCountStats();
    }

  public DataSet getFitnessStats()
    {
    return fitnessStats;
    }

  public DataSet getSizeStats()
    {
    return sizeStats;
    }

  public DataSet getBpStats()
    {
    return bpStats;
    }

  public void outputCurrentStats()
    {
    String[] titles = new String[]{"--- Num BPs/candidate ---",   "--- Evaluator ---"};

    DataSet[] allStats = new DataSet[]{sizeStats, fitnessStats};

    StringBuffer buff = new StringBuffer();
    for(int i=0; i<allStats.length; i++)
      {
      DataSet stats = allStats[i];

      buff.append(titles[i] + "\n");
      buff.append("\tMin: " + stats.getMinimum() + "\tMax: " + stats.getMaximum() + "\tMean: " + stats.getArithmeticMean() + "\tSD: " + stats.getStandardDeviation() + "\n");
      }
    log.info("\n" + buff);
    }

  private void populationFitnessStats()
    {
    fitnessStats = new CandidateEvaluation().getStatistics(this.population);
    }

  private void sizePopulationSizeStats()
    {
    sizeStats = new CandidateBreakpoints().getStatistics(this.population);
    }

  private void breakpointCountStats()
    {
    //bpStats = new BreakpointRepresentation().getStatistics(this.population);
    }



  }
