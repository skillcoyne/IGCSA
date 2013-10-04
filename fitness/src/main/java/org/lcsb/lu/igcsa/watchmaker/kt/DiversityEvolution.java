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
import org.uncommons.watchmaker.framework.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiversityEvolution<T> extends AbstractEvolutionEngine<T>
  {
  static Logger log = Logger.getLogger(DiversityEvolution.class.getName());

  private final EvolutionaryOperator<T> evolutionScheme;
  private final SelectionStrategy<? super T> selectionStrategy;
  private final FitnessEvaluator<? super T> fitnessEvaluator;


  public DiversityEvolution(CandidateFactory<T> candidateFactory, FitnessEvaluator<? super T> fitnessEvaluator, EvolutionaryOperator<T> evolutionScheme, SelectionStrategy<? super T> selectionStrategy, Random rng)
    {
    super(candidateFactory, fitnessEvaluator, rng);
    this.fitnessEvaluator = fitnessEvaluator;
    this.evolutionScheme = evolutionScheme;
    this.selectionStrategy = selectionStrategy;
    }


  /*
  This is selecting and evolving the entire population each time.
   */
  @Override
  protected List<EvaluatedCandidate<T>> nextEvolutionStep(List<EvaluatedCandidate<T>> evaluatedPopulation, int eliteCount, Random rng)
    {
    // Select candidates to evolve
    List<T> population = selectionStrategy.select(evaluatedPopulation, fitnessEvaluator.isNatural(), evaluatedPopulation.size(), rng);
//    CandidateUtils.testForDuplication((List<? extends KaryotypeCandidate>) population);

    log.info(population.size() + " selected for evolution step");

    // Run evolution
    population = evolutionScheme.apply(population, rng);
//    CandidateUtils.testForDuplication((List<? extends KaryotypeCandidate>) population);

    // I'm not doing anything with elites, so ignoring that.
    // return the evaluated population
    return evaluatePopulation(population);
    }




  }
