/**
 * org.lcsb.lu.igcsa.watchmaker.kt.statistics
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt.statistics;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import java.util.List;

public class CandidateBreakpoints extends AbstractPopulationStatistic
  {
  static Logger log = Logger.getLogger(CandidateBreakpoints.class.getName());


  public CandidateBreakpoints()
    {}

  public CandidateBreakpoints(double min)
    {
    this.setExpected(Statistic.MIN, min);
    }

  @Override
  public DataSet getStatistics(List<EvaluatedCandidate<KaryotypeCandidate>> population)
    {
    DataSet sizeStats = new DataSet(population.size());
    for (EvaluatedCandidate<KaryotypeCandidate> candidate: population)
      sizeStats.addValue(candidate.getCandidate().getBreakpoints().size()); // doesn't include anything about aneuploidy

    return sizeStats;
    }
  }
