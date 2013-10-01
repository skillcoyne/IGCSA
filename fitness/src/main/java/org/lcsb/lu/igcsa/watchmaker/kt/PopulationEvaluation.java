package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
//    Map<Set<Band>, Integer> same = new HashMap<Set<Band>, Integer>();
//    for (EvaluatedCandidate<? extends KaryotypeCandidate> ind : population)
//      {
//      if (same.containsKey(ind.getCandidate()))
//        same.put((Set<Band>) ind.getCandidate(), same.get(ind.getCandidate()) + 1);
//      else
//        same.put((Set<Band>) ind.getCandidate(), 1);
//      }
//
//    uniqueStats = new DataSet(same.size());
//    for (Integer val : same.values())
//      uniqueStats.addValue(val);
    }


  }
