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
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.*;

public class Mutator implements EvolutionaryOperator<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(Mutator.class.getName());

  private double MR = 0.3;
  private double proportion = 0.05;

  private final Range MRLIMIT = new DoubleRange(0, 1);
  private CandidateFactory<KaryotypeCandidate> factory;

  public Mutator(double MR, double populationProportion, CandidateFactory<KaryotypeCandidate> factory)
    {
    if (!MRLIMIT.containsDouble(MR))
      throw new IllegalArgumentException("MR must be between 0,1");

    this.MR = MR;
    this.proportion = populationProportion;
    this.factory = factory;
    }



  @Override
  public List<KaryotypeCandidate> apply(List<KaryotypeCandidate> candidates, Random random)
    {
    log.info("Apply mutation to " + candidates.size());
    if (random.nextDouble() <= MR)
      {
      // Choose some individuals to randomly mutate.
      int numToMutate = random.nextInt((int) Math.round(candidates.size() * proportion));
      Collections.shuffle(candidates, random);

      log.info("Mutating " + numToMutate + " individuals");

      List<KaryotypeCandidate> mutatedIndividuals = new ArrayList<KaryotypeCandidate>();
      for (int i = 0; i < numToMutate; i++)
        {
        KaryotypeCandidate individual = candidates.get(i).clone();
        //candidates.remove(i);

        // each individual gets a different set of bands & aneuploidies to mutate
        KaryotypeCandidate randomCand = factory.generateRandomCandidate(random);

        // either add or remove the breakpoints.
        for (Band band: randomCand.getBreakpoints())
          {
          // either add or remove it from the set
          if (individual.hasBreakpoint(band))
            individual.removeBreakpoint(band);
          else
            individual.addBreakpoint(band);
          }

        // maybe we do ploidy differently? Or not at all...
//        for (KaryotypeCandidate.Aneuploidy pdy: randomCand.getAneuploidies())
//          {
//          if (individual.getAneuploidies().contains(pdy))
//            {
//            if (pdy.isGain())
//              individual.gainChromosome(pdy.getChromosome());
//            else
//              individual.loseChromosome(pdy.getChromosome());
//            }
//          else
//            individual.addAneuploidy(pdy);
//          }

        mutatedIndividuals.add(individual);
        Collections.shuffle(candidates, random); // ensures a random selection of individuals
        // NOTE: What I'm not doing is checking fitness of these mutated individuals.  What I could do is create entirely new mutated individuals, add them to the population then do elite selection.
        }
      candidates.addAll(mutatedIndividuals);
      }



    return candidates;
    }
  }
