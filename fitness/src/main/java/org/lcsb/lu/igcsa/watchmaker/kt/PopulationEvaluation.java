package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

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

  private List<EvaluatedCandidate<? extends KaryotypeCandidate>> population;

  private DataSet fitnessStats;
  private DataSet sizeStats;
  private DataSet uniqueStats;
  private DataSet complexityStats;

  public PopulationEvaluation(List<EvaluatedCandidate<? extends KaryotypeCandidate>> candidateList)
    {
    this.population = candidateList;

    populationFitnessStats();
    sizePopulationSizeStats();
    complexityPopulationStats();
    uniqueIndividualStats();
    }

  public DataSet getFitnessStats()
    {

    return fitnessStats;
    }

  public DataSet getSizeStats()
    {
    return sizeStats;
    }

  public DataSet getUniqueStats()
    {
    return uniqueStats;
    }

  public DataSet getComplexityStats()
    {
    return complexityStats;
    }

  public void outputCurrentStats()
    {
    String[] titles = new String[]{"--- BP Size ---", "--- Fitness ---", "--- Complexity ---", "--- Uniqueness ---"};
    DataSet[] allStats = new DataSet[]{sizeStats, fitnessStats, complexityStats, uniqueStats};

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


  private void complexityPopulationStats()
    {
    /* so...how could we measure complexity. Simply number of breakpoints (as these are not duplicated in an individual) + number of
     aneuploidies? Means an individual with 3 bps and 2 aneuploidies has the same complexity as an individual with 5bps? Or should these
     be somehow weighted with the fitness score as well?
     */
    complexityStats = new DataSet(population.size());
    for (EvaluatedCandidate<? extends KaryotypeCandidate> candidate: population)
      {
      complexityStats.addValue( candidate.getFitness() *
                                (candidate.getCandidate().getBreakpoints().size() + candidate.getCandidate().getAneuploidies().size())  );
      }
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
    for (EvaluatedCandidate<? extends KaryotypeCandidate> ind: population)
      sizeStats.addValue(ind.getCandidate().getBreakpoints().size()); // doesn't include anything about aneuploidy
    }


  private void uniqueIndividualStats()
    {
    Map<Set<Band>, Integer> same = new HashMap<Set<Band>, Integer>();
    for (EvaluatedCandidate<? extends KaryotypeCandidate> ind : population)
      {
      List<Band> bands = new ArrayList<Band>(ind.getCandidate().getBreakpoints());
      Collections.sort(bands);
      if (same.containsKey(ind.getCandidate().getBreakpoints()))
        same.put((Set<Band>) ind.getCandidate().getBreakpoints(), same.get(ind.getCandidate().getBreakpoints()) + 1);
      else
        same.put((Set<Band>) ind.getCandidate().getBreakpoints(), 1);
      }
    Set<Integer> freq = new HashSet<Integer>(same.values());

    uniqueStats = new DataSet(freq.size());
    for (Integer val : freq)
      uniqueStats.addValue(val);
    }


  }
