/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionUtils;
import org.uncommons.watchmaker.framework.SelectionStrategy;

import java.util.*;

public class KaryotypeSelectionStrategy implements SelectionStrategy<Object>
  {
  static Logger log = Logger.getLogger(SelectionStrategy.class.getName());

  private double maxFitness = 2.5;

  public KaryotypeSelectionStrategy(double maxFitness)
    {
    this.maxFitness = maxFitness;
    }

  //@Override
  public <S> List<S> select(List<EvaluatedCandidate<S>> population, boolean naturalFitnessScores, int selectionSize, Random rng)
    {
    CandidateGraph cg = CandidateGraph.getInstance();

    // remove the candidates with the highest fitness scores
    List<EvaluatedCandidate<S>> evaluatedCandidates = new ArrayList<EvaluatedCandidate<S>>();
    //log.info("Selecting the top " + selectionSize + " from population " + population.size());
    int newCandidates = 0;
    EvolutionUtils.sortEvaluatedPopulation(population, true);
    for (EvaluatedCandidate<S> candidate: population)
      {
      if (candidate.getFitness() < maxFitness)
        evaluatedCandidates.add(candidate);
      else
        {
        ++newCandidates;
        cg.removeNode((KaryotypeCandidate) candidate.getCandidate());
        }
      }
    log.info("Removed " + newCandidates + " candidates. Population=" + evaluatedCandidates.size());


    // instead, for every pair that has a high similarity roll a die and select only one (a sort of Tournament selection)
    List<KaryotypeCandidate> removeFromPopulation = new ArrayList<KaryotypeCandidate>();
    double similarityScore = 0.08;

    Iterator<DefaultWeightedEdge> eI = cg.weightSortedEdgeIterator();
    while (eI.hasNext())
      {
      DefaultWeightedEdge edge = eI.next();
      if (cg.getEdgeWeight(edge) <= similarityScore)
        {
        KaryotypeCandidate notSelected = cg.getNodes(edge).get(rng.nextInt(2));
        cg.removeNode(notSelected);
        removeFromPopulation.add(notSelected);
        }
      }

    log.info("Removing " + removeFromPopulation.size() + " from population");

    // get rid of anything with < min fitness
//    Iterator<EvaluatedCandidate<S>> pI = population.iterator();
//    while (pI.hasNext())
//      if (pI.next().getFitness() <= minFitness) pI.remove();
//
//    // avoid any array errors
//    if (selectionSize > population.size())
//      selectionSize = population.size();

    List<S> selection = new ArrayList<S>();
    for(EvaluatedCandidate<S> candidate: evaluatedCandidates)
      {
      KaryotypeCandidate kc = (KaryotypeCandidate) candidate.getCandidate();
      if (!removeFromPopulation.contains(kc))
        selection.add(candidate.getCandidate());
      }




    //    List<S> selection = new ArrayList<S>(selectionSize);
//    for (int i=0; i<selectionSize; i++)
//      selection.add(population.get(i).getCandidate());

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