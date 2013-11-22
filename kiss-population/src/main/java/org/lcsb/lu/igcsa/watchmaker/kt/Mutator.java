/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.generator.Aneuploidy;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.*;

public class Mutator implements EvolutionaryOperator<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(Mutator.class.getName());

  private double MR = 0.3;
  private double proportion = 0.05;

  private final Range MRLIMIT = new DoubleRange(0, 1);
  private CandidateFactory<KaryotypeCandidate> factory;
  private FitnessEvaluator<KaryotypeCandidate> evaluator;

  public Mutator(double MR, double populationProportion, CandidateFactory<KaryotypeCandidate> factory, FitnessEvaluator<KaryotypeCandidate> evaluator)
    {
    if (!MRLIMIT.containsDouble(MR))
      throw new IllegalArgumentException("MR must be between 0,1");

    this.MR = MR;
    this.proportion = populationProportion;
    this.factory = factory;
    this.evaluator = evaluator;
    }


  @Override
  public List<KaryotypeCandidate> apply(List<KaryotypeCandidate> candidates, Random random)
    {
    // Choose a proportion of the population to possibly mutate
    int numToMutate = random.nextInt((int) Math.round(candidates.size() * proportion));
    Collections.shuffle(candidates, random);

    List<KaryotypeCandidate> mutatedIndividuals = new ArrayList<KaryotypeCandidate>();

    for (int i = 0; i < numToMutate; i++)
      {
      if (random.nextDouble() <= MR)
        {
        KaryotypeCandidate current = candidates.get(i);
        KaryotypeCandidate individualToMutate = current.clone();

        candidates.remove(i);
        CandidateGraph.getGraph().removeNode(current);

        // each individual gets a different set of bands & aneuploidies to mutate
        KaryotypeCandidate randomCand = factory.generateRandomCandidate(random);
        flipBreakpoints(individualToMutate, randomCand);
        flipPloidy(individualToMutate, randomCand);

        mutatedIndividuals.add(individualToMutate);
        Collections.shuffle(candidates, random); // ensures a random selection of individuals
        }
      }
    log.info("Mutated " + mutatedIndividuals.size());

    candidates.addAll(mutatedIndividuals);

    long start = System.currentTimeMillis();
    for (KaryotypeCandidate mut : mutatedIndividuals)
      CandidateGraph.updateGraph(mut, candidates);
    log.info("graph update took : " + (System.currentTimeMillis() - start) + " ms");

    return candidates;
    }

  // Flip the breakpoints present in the selected candidate based on the new, randomly generated candidate
  private void flipBreakpoints(KaryotypeCandidate individual, KaryotypeCandidate randomCand)
    {
    for (Band band : randomCand.getBreakpoints())
      individual.addBreakpoint(band);  // just add the breakpoints
    }

  // adds to existing aneuploidies or adds new ones
  private void flipPloidy(KaryotypeCandidate individual, KaryotypeCandidate randomCand)
    {
    // maybe we do ploidy differently? Or not at all...
    for (Aneuploidy pdy : randomCand.getAneuploidies())
      {
      if (individual.getAneuploidies().contains(pdy))
        {
        if (pdy.getCount() > 0)
          individual.gainChromosome(pdy.getChromosome());
        else
          individual.loseChromosome(pdy.getChromosome());
        }
      else
        individual.addAneuploidy(pdy);
      }
    }


  }
