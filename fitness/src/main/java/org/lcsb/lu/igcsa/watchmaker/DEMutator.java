/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.prob.Probability;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.*;

public class DEMutator implements EvolutionaryOperator<Set<Band>>
  {
  static Logger log = Logger.getLogger(DEMutator.class.getName());

  private double MR = 0.3;
  private double proportion = 0.05;

  private final Range MRLIMIT = new DoubleRange(0, 1);
  private CandidateFactory<Set<Band>> factory;

  public DEMutator(double MR, double populationProportion, CandidateFactory<Set<Band>> factory)
    {
    if (!MRLIMIT.containsDouble(MR))
      throw new IllegalArgumentException("MR must be between 0,1");

    this.MR = MR;
    this.proportion = populationProportion;
    this.factory = factory;
    }

  @Override
  public List<Set<Band>> apply(List<Set<Band>> candidates, Random random)
    {
    if (random.nextDouble() < MR)
      {
      // Choose some individuals to randomly mutate.
      int numToMutate = random.nextInt((int) Math.round(candidates.size() * proportion));
      Collections.shuffle(candidates);

      log.info("Mutating " + numToMutate + " individuals");

      List<Set<Band>> mutatedIndividuals = new ArrayList<Set<Band>>();
      for (int i = 0; i < numToMutate; i++)
        {
        Set<Band> individual = candidates.get(i);
        candidates.remove(i);

        // each individual gets a different set of bands to mutate
        Set<Band> randomCand = factory.generateRandomCandidate(random); // select breakpoints to flip
        for (Band band : randomCand)
          {
          // either add or remove it from the set
          if (individual.contains(band))
            individual.remove(band);
          else
            individual.add(band);
          }

        mutatedIndividuals.add(individual);
        Collections.shuffle(candidates);
        }
      candidates.addAll(mutatedIndividuals);
      }

    return candidates;
    }

  }
