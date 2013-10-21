package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
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

  private List<EvaluatedCandidate<KaryotypeCandidate>> population;
  //private List<EvaluatedCandidate<? extends KaryotypeCandidate>> population;

  private DataSet fitnessStats;
  private DataSet sizeStats;
  private DataSet complexityStats;
  private DataSet bpStats;

  public PopulationEvaluation(List<EvaluatedCandidate<KaryotypeCandidate>> candidateList)
    {
    this.population = candidateList;

    populationFitnessStats();
    sizePopulationSizeStats();
    complexityPopulationStats();
    breakpointCountStats();
    }

  public DataSet getFitnessStats()
    {
    return fitnessStats;
    }

  public DataSet getSizeStats()
    {
    return sizeStats;
    }

  public DataSet getComplexityStats()
    {
    return complexityStats;
    }

  public DataSet getBpStats()
    {
    return bpStats;
    }

  public void outputCurrentStats()
    {
    String[] titles = new String[]{"--- BP Size ---", "--- Complexity ---", "--- Rep'd BPs ---"};
    DataSet[] allStats = new DataSet[]{sizeStats,  complexityStats, bpStats};

    StringBuffer buff = new StringBuffer();
    for(int i=0; i<allStats.length; i++)
      {
      DataSet stats = allStats[i];

      buff.append(titles[i] + "\n");
      buff.append("\tMin: " + stats.getMinimum() + "\tMax: " + stats.getMaximum() + "\tMean: " + stats.getArithmeticMean() + "\tSD: " + stats.getStandardDeviation() + "\n");
      buff.append("\tDispersion: " + stats.getStandardDeviation() / stats.getArithmeticMean() + "\n");
      }
    log.info("\n" + buff);
    }



  private void complexityPopulationStats()
    {
    /* so...how could we measure complexity. Simply number of breakpoints (as these are not duplicated in an individual) + number of
     aneuploidies? Means an individual with 3 bps and 2 aneuploidies has the same complexity as an individual with 5bps?
     */
    complexityStats = new DataSet(population.size());
    for (EvaluatedCandidate<? extends KaryotypeCandidate> candidate: population)
      complexityStats.addValue( (candidate.getCandidate().getBreakpoints().size() + candidate.getCandidate().getAneuploidies().size()) );
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

  private void breakpointCountStats()
    {
    BreakpointWatcher.getWatcher().reset();

    for(EvaluatedCandidate<KaryotypeCandidate> ind: population)
      {
      for(Band b: ind.getCandidate().getBreakpoints())
        BreakpointWatcher.getWatcher().add(b);
      }

    bpStats = new DataSet(BreakpointWatcher.getWatcher().getBreakpointCounts().size());
    for (Map.Entry<Band, Integer> entry: BreakpointWatcher.getWatcher().getBreakpointCounts().entrySet())
      bpStats.addValue(entry.getValue());

    }



  }
