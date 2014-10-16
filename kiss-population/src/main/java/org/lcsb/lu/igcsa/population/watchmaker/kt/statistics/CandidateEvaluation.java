/**
 * org.lcsb.lu.igcsa.population.watchmaker.kt.statistics
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.population.watchmaker.kt.statistics;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.population.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import java.util.List;

public class CandidateEvaluation extends AbstractPopulationStatistic
  {
  static Logger log = Logger.getLogger(CandidateEvaluation.class.getName());


  @Override
  public DataSet getStatistics(List<EvaluatedCandidate<KaryotypeCandidate>> population)
    {
    DataSet fitnessStats = new DataSet(population.size());

    for (EvaluatedCandidate<?> candidate : population)
      fitnessStats.addValue(candidate.getFitness());

    return fitnessStats;
    }
  }
