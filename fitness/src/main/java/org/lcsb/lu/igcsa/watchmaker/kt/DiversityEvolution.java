/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.uncommons.watchmaker.framework.*;

import java.util.*;

public class DiversityEvolution<T extends KaryotypeCandidate> extends AbstractEvolutionEngine<T>
  {
  static Logger log = Logger.getLogger(DiversityEvolution.class.getName());

  private final EvolutionaryOperator<T> evolutionScheme;
  private final SelectionStrategy<? super T> selectionStrategy;
  private final FitnessEvaluator<? super T> fitnessEvaluator;
  private final CandidateFactory<T> candidateFactory;
  private final int maxPopulationSize;

  public DiversityEvolution(CandidateFactory<T> candidateFactory, FitnessEvaluator<? super T> fitnessEvaluator, EvolutionaryOperator<T> evolutionScheme, SelectionStrategy<? super T> selectionStrategy, Random rng, int maxPopulationSize)
    {
    super(candidateFactory, fitnessEvaluator, rng);
    this.candidateFactory = candidateFactory;
    this.fitnessEvaluator = fitnessEvaluator;
    this.evolutionScheme = evolutionScheme;
    this.selectionStrategy = selectionStrategy;
    this.maxPopulationSize = maxPopulationSize;
    }

  /*
  This is selecting and evolving the entire population each time.
   */
  @Override
  protected List<EvaluatedCandidate<T>> nextEvolutionStep(List<EvaluatedCandidate<T>> evaluatedPopulation, int eliteCount, Random rng)
    {
    // Select candidates to evolve
    List<T> population = selectionStrategy.select(evaluatedPopulation, fitnessEvaluator.isNatural(), evaluatedPopulation.size(), rng);
    log.info(population.size() + " selected for next step. Adding " + (maxPopulationSize - population.size() + " random individuals into the population."));
    log.info("Graph: " + CandidateGraph.getGraph().nodeCount() + " " + CandidateGraph.getGraph().edgeCount());

    while (population.size() < maxPopulationSize)
      {
      T candidate = candidateFactory.generateRandomCandidate(rng);
      population.add(candidate);
      CandidateGraph.updateGraph(candidate, (List<KaryotypeCandidate>) population);
      }

    // Run evolution (XO, mutation)
    population = evolutionScheme.apply(population, rng);

    return evaluatePopulation(population);
    }


  }
