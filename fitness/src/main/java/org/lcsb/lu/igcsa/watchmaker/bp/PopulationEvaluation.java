/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.bp;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PopulationEvaluation
  {
  static Logger log = Logger.getLogger(PopulationEvaluation.class.getName());

  private List<? extends EvaluatedCandidate<? extends Set<Band>>> population;

  private DataSet sizeStats;
  private DataSet fitnessStats;
  private DataSet uniqueStats;

  public PopulationEvaluation(List<? extends EvaluatedCandidate<? extends Set<Band>>> population)
    {
    this.population = population;

    sizePopulationSizeStats();
    uniqueIndividualStats();
    populationFitnessStats();
    }

  public void outputCurrentStats()
    {
    String[] titles = new String[]{"--- Size ---", "--- Fitness ---", "--- Uniqueness ---"};
    DataSet[] allStats = new DataSet[]{sizeStats, fitnessStats, uniqueStats};

    StringBuffer buff = new StringBuffer();
    for(int i=0; i<allStats.length; i++)
      {
      DataSet stats = allStats[i];

      buff.append("\n" + titles[i] + "\n");
      buff.append("\tMin: " + stats.getMinimum() + "\tMax: " + stats.getMaximum() + "\tMean: " + stats.getArithmeticMean() + "\tSD: " + stats.getStandardDeviation() + "\n");
      buff.append("\tDispersion: " + stats.getStandardDeviation() / stats.getArithmeticMean() + "\n");
      }
    log.info(buff);
    }


  public DataSet getSizeStats()
    {
    return sizeStats;
    }

  public DataSet getFitnessStats()
    {
    return fitnessStats;
    }

  public DataSet getUniqueStats()
    {
    return uniqueStats;
    }

  private void populationFitnessStats()
    {
    fitnessStats = new DataSet(population.size());

    for (EvaluatedCandidate<?> candidate : population)
      fitnessStats.addValue(candidate.getFitness());
    }

  private void sizePopulationSizeStats()
    {
    sizeStats = new DataSet(population.size());
    for (EvaluatedCandidate<?> ind: population)
      {
      Set<Band> candidate = (Set<Band>) ind.getCandidate();
      sizeStats.addValue(candidate.size());
      }
    }


  private void uniqueIndividualStats()
    {
    Map<Set<Band>, Integer> same = new HashMap<Set<Band>, Integer>();
    for (EvaluatedCandidate<?> ind : population)
      {
      if (same.containsKey(ind.getCandidate()))
        same.put((Set<Band>) ind.getCandidate(), same.get(ind.getCandidate()) + 1);
      else
        same.put((Set<Band>) ind.getCandidate(), 1);
      }

    uniqueStats = new DataSet(same.size());
    for (Integer val : same.values())
      uniqueStats.addValue(val);
    }

  }
