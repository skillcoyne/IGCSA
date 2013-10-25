package org.lcsb.lu.igcsa.watchmaker.kt.statistics;

import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;

import java.util.List;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public interface PopulationStatistic
  {

  public void setExpected(Statistic s, double v);

  public Map<Statistic, Double> getExpectedStatistics();

  public double getExpected(Statistic s);

  public abstract DataSet getStatistics(List<EvaluatedCandidate<KaryotypeCandidate>> candidates);

  }
