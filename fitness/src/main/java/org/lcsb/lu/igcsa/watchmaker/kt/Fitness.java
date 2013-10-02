package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.utils.CandidateUtils;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Fitness implements FitnessEvaluator<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(Fitness.class.getName());

  private Probability bpProb;
  private Probability gainLossProb;
  private Probability ploidyCountProb;
  private Probability bpCountProb;

  private boolean naturalFitness = false;

  private Map<Band, Double> breakpointProbs = new HashMap<Band, Double>();

  double sumProb = 0;

  public Fitness(Probability bpProb, Probability bpCountProb, Probability gainLossProb, Probability ploidyCountProb, boolean naturalFitness)
    {
    this.bpProb = bpProb;
    this.gainLossProb = gainLossProb;
    this.ploidyCountProb = ploidyCountProb;
    this.bpCountProb = bpCountProb;
    this.naturalFitness = naturalFitness;


    // adjust the breakpoint probabilities
    for (Map.Entry<Object, Double> entry : this.bpProb.getRawProbabilities().entrySet())
      {
      this.breakpointProbs.put((Band) entry.getKey(), entry.getValue() * 100);
      this.sumProb += entry.getValue();
      }
    }

  //  public Fitness(Probability bpProb, Probability gainLossProb)
  //    {
  //    // Use probabilities to evaluate fitness..worked well enough with breakpoints. Need to see what happens with aneuploidy.
  //    this.bpProb = bpProb;
  //    this.gainLossProb = gainLossProb;
  //
  //    }

  @Override
  public double getFitness(KaryotypeCandidate karyotypeCandidate, List<? extends KaryotypeCandidate> karyotypeCandidates)
    {
    // not using the list of candidates at the moment. I could integrate into the fitness score a population level score rather than leaving that to the termination criteria.  Have to think about how as it would perhaps simplify the code somewhat.

    double bpScore = 0.0;
    for (Band band : karyotypeCandidate.getBreakpoints())
      bpScore += this.breakpointProbs.get(band); // these are adjusted values

    // adjust the score for the number of breakpoints as well
    bpScore = adjustByCount(bpCountProb.getRawProbabilities(), bpScore, karyotypeCandidate.getBreakpoints().size());

    // while technically a genome with no mutations is "fit" I'm looking for genomes with at least some mutations
    if (bpScore == 0)
      bpScore = 200;

    // it's ok if there are no aneuploidies, so no adjustment for a 0 score.
    // What is a problem is that
    double apScore = 0.0;
    for (KaryotypeCandidate.Aneuploidy aneuploidy : karyotypeCandidate.getAneuploidies())
      apScore += gainLossProb.getRawProbabilities().get(aneuploidy.getChromosome());// * Math.abs(aneuploidy.getCount());

    apScore = adjustByCount(ploidyCountProb.getRawProbabilities(), apScore, karyotypeCandidate.getAneuploidies().size());

    return bpScore + apScore;
    }

  private double adjustByCount(Map<Object, Double> countProbs, double score, int count)
    {
    for (Map.Entry<Object, Double> entry : countProbs.entrySet())
      {
      // weighted by the inverse probability of that number of things (breakpoints/aneuploidies) occurring
      IntRange range = (IntRange) entry.getKey();
      if (range.containsInteger(count))
        {
        score = score * Math.abs(Math.log(entry.getValue()));
        break;
        }
      }
    return score;
    }


  // Higher scores are naturally fit for watchmaker;
  @Override
  public boolean isNatural()
    {
    return naturalFitness;
    }
  }
