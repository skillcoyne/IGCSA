package org.lcsb.lu.igcsa.watchmaker.kt;

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
  private Probability ploidyProb;

  private Map<Band, Double> breakpointProbs = new HashMap<Band, Double>();

  double sumProb = 0;

  public void Fitness(Probability bpProb, Probability ploidyProb)
    {
    // Use probabilities to evaluate fitness..worked well enough with breakpoints. Need to see what happens with aneuploidy.
    this.bpProb = bpProb;
    this.ploidyProb = ploidyProb;

    for (Map.Entry<Object, Double> entry : this.bpProb.getRawProbabilities().entrySet())
      {
      breakpointProbs.put((Band) entry.getKey(), entry.getValue()*100);
      sumProb += entry.getValue();
      }

    }

  @Override
  public double getFitness(KaryotypeCandidate karyotypeCandidate, List<? extends KaryotypeCandidate> karyotypeCandidates)
    {
    double score = 0.0;
    // not using the list of candidates at the moment. I could integrate into the fitness score a population level score rather than leaving that to the termination criteria.  Have to think about how as it would perhaps simplify the code somewhat.

    double bpScore = 0.0;
    for (Band band : karyotypeCandidate.getBreakpoints())
      bpScore += breakpointProbs.get(band); // these are adjusted values

    // while technically a genome with no mutations is "fit" I'm looking for genomes with at least some mutations
    if (bpScore == 0)
      bpScore = 200;

    // it's ok if there are no aneuploidies, so no adjustment for a 0 score.
    double apScore = 0.0;
    for(KaryotypeCandidate.Aneuploidy aneuploidy: karyotypeCandidate.getAneuploidies())
      apScore += ploidyProb.getRawProbabilities().get(aneuploidy.getChromosome()) * Math.abs(aneuploidy.getCount());

    return bpScore + apScore;
    }

  @Override
  public boolean isNatural()
    {
    return false;
    }
  }
