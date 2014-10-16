/**
 * org.lcsb.lu.igcsa.population.watchmaker.kt.statistics
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.population.watchmaker.kt.statistics;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPopulationStatistic implements PopulationStatistic
  {
  static Logger log = Logger.getLogger(AbstractPopulationStatistic.class.getName());


  private Map<Statistic, Double> expectedStats = new HashMap<Statistic, Double>();

  public void setExpected(Statistic s, double v)
    {
    this.expectedStats.put(s, v);
    }


  public Map<Statistic, Double> getExpectedStatistics()
    {
    return this.expectedStats;
    }


  public double getExpected(Statistic s)
    {
    return this.expectedStats.get(s);
    }


  }
