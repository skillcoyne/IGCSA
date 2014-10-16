/**
 * org.lcsb.lu.igcsa.population.watchmaker.kt.statistics
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.population.watchmaker.kt.statistics;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.population.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreakpointRepresentation extends AbstractPopulationStatistic
  {
  static Logger log = Logger.getLogger(BreakpointRepresentation.class.getName());

  private Collection<Band> possibleBreakpoints;

  public BreakpointRepresentation(Collection<Band> bands, double min)
    {
    this.setExpected(Statistic.MIN, min);
    this.possibleBreakpoints = bands;
    }


  @Override
  public DataSet getStatistics(List<EvaluatedCandidate<KaryotypeCandidate>> candidates)
    {
    Map<Band, Integer> breakpoints = new HashMap<Band, Integer>();
    for (Band band : this.possibleBreakpoints)
      breakpoints.put(band, 0);


    DataSet bpStats = new DataSet(breakpoints.size());

    for (EvaluatedCandidate<KaryotypeCandidate> candidate: candidates)
      {
      for (Band band: candidate.getCandidate().getBreakpoints())
        breakpoints.put(band, breakpoints.get(band)+1);
      }

    for (Map.Entry<Band, Integer> entry: breakpoints.entrySet())
      bpStats.addValue(entry.getValue());

    return bpStats;
    }
  }
