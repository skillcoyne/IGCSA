/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.utils.CandidateUtils;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionUtils;
import org.uncommons.watchmaker.framework.SelectionStrategy;

import java.util.*;

public class KaryotypeSelectionStrategy implements SelectionStrategy<Object>
  {
  static Logger log = Logger.getLogger(SelectionStrategy.class.getName());

  private int maxSelectionSize = 100;
  private double minFitness = 0.0;

  public KaryotypeSelectionStrategy(int maxSelectionSize, double minFitness)
    {
    this.maxSelectionSize = maxSelectionSize;
    this.minFitness = minFitness;
    }

  //@Override
  public <S> List<S> select(List<EvaluatedCandidate<S>> population, boolean naturalFitnessScores, int selectionSize, Random rng)
    { // for the moment this is elite, but it will return everything
    if (selectionSize > maxSelectionSize)
      selectionSize = maxSelectionSize;

    log.info("Selecting the top " + selectionSize + " from population " + population.size());
    EvolutionUtils.sortEvaluatedPopulation(population, false);

    // get rid of anything with < min fitness
    Iterator<EvaluatedCandidate<S>> pI = population.iterator();
    while (pI.hasNext())
      if (pI.next().getFitness() <= minFitness) pI.remove();

    // avoid any array errors
    if (selectionSize > population.size())
      selectionSize = population.size();

    List<S> selection = new ArrayList<S>(selectionSize);
    for (int i=0; i<selectionSize; i++)
      selection.add(population.get(i).getCandidate());

    return selection;
    }


  //@Override
  public <S> List<S> select2(List<EvaluatedCandidate<S>> population, boolean naturalFitnessScores, int selectionSize, Random rng)
    {
    // randomize
    Collections.shuffle(population, rng);



    return null;
    }


  }




/*
The selection strategy needs to ensure a diversity, rather than a convergence.  However, we also need to get rid of extremely unfit individuals.
So perhaps a two part selection?
1. Take the top 75% of the population based on fitness.
2. Maybe here cluster the population into sets based on similarity measures (which I have not yet worked out) then pick one from each cluster?  This might be similar to island evolution.
*/