/**
 * org.lcsb.lu.igcsa.watchmaker.kt.statistics
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt.statistics;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.watchmaker.kt.BreakpointWatcher;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;

import java.util.List;
import java.util.Map;

public class BreakpointRepresentation extends AbstractPopulationStatistic
  {
  static Logger log = Logger.getLogger(BreakpointRepresentation.class.getName());

  public BreakpointRepresentation()
    {}

  public BreakpointRepresentation(double min)
    {
    this.setExpected(Statistic.MIN, min);
    }


  @Override
  public DataSet getStatistics(List<EvaluatedCandidate<KaryotypeCandidate>> candidates)
    {
    DataSet bpStats = new DataSet(BreakpointWatcher.getWatcher().getBreakpointCounts().size());
    for (Map.Entry<Band, Integer> entry: BreakpointWatcher.getWatcher().getBreakpointCounts().entrySet())
      bpStats.addValue(entry.getValue());

    return bpStats;
    }
  }
