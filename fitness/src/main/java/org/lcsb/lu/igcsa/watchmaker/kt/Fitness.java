package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.utils.CandidateUtils;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.*;


/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Fitness implements FitnessEvaluator<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(Fitness.class.getName());

  private final Probability bpProb;
  private final Probability gainLossProb;
  private final Probability ploidyCountProb;
  private final Probability bpCountProb;

  private boolean naturalFitness = false;

  private Map<Band, Double> breakpointProbs = new HashMap<Band, Double>();

  private double sumProb = 0;

  /*
  Fitness could be calcuated in a few ways.  Straightforwardly count the ploidy and breakpoints.  Higher number is lower fitness.
  These could be adjusted for the likelihood of occurrence.  For instance, 9q34 is a very common breakpoint and so has a high fitness, where
  Xq25 has a fairly low probability of occurrence and thus a lower fitness.

  Same happens with the gain/loss of a chromosome.

   */

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
      this.breakpointProbs.put((Band) entry.getKey(), Math.abs(Math.log(entry.getValue()))/100 );
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

  public double getBreakpointScore(KaryotypeCandidate karyotypeCandidate)
    {
    double score = 0.0;
    Set<String> chromosomes = new HashSet<String>();
    for (Band band : karyotypeCandidate.getBreakpoints())
      {
      score += this.breakpointProbs.get(band); // these are adjusted values
      chromosomes.add(band.getChromosomeName());
      }

    // adjust the score for the number of breakpoints as well
    //score = adjustByCount(bpCountProb.getRawProbabilities(), score, karyotypeCandidate.getBreakpoints().size());
    score += (double)chromosomes.size()/(double)karyotypeCandidate.getBreakpoints().size();

    // while technically a genome with no mutations is "fit" I'm looking for genomes with at least some mutations
    if (score  == 0)
      log.info(karyotypeCandidate + " fitness 0");
    //      this.breakpointScore  = 200;

    return score;
    }

  public double getAneuploidyScore(KaryotypeCandidate karyotypeCandidate)
    {
    // it's ok if there are no aneuploidies, so no adjustment for a 0 score.
    // What is a problem is that
    double score = 0.0;

    Set<String> chromosomes = new HashSet<String>();
    for (KaryotypeCandidate.Aneuploidy aneuploidy : karyotypeCandidate.getAneuploidies())
      {
      score += Math.abs(Math.log(gainLossProb.getRawProbabilities().get(aneuploidy.getChromosome())))/10;
      chromosomes.add(aneuploidy.getChromosome());
      }

    //score = adjustByCount(ploidyCountProb.getRawProbabilities(), score, karyotypeCandidate.getAneuploidies().size());
    if (score > 0)
      score += (double)chromosomes.size()/(double)karyotypeCandidate.getAneuploidies().size();

    return score;
    }


  @Override
  public double getFitness(KaryotypeCandidate karyotypeCandidate, List<? extends KaryotypeCandidate> karyotypeCandidates)
    {
    double similaritySum = 0.0;

    if (karyotypeCandidates.size() > CandidateGraph.getInstance().nodeCount())
      throw new RuntimeException("Graph has not been updated correctly, " + CandidateGraph.getInstance().nodeCount() + " nodes found, " + karyotypeCandidates.size() + " expected");

    Iterator<DefaultWeightedEdge> eI = CandidateGraph.getInstance().getEdges(karyotypeCandidate).iterator();
    while (eI.hasNext())
      similaritySum += CandidateGraph.getInstance().getEdgeWeight(eI.next());

    similaritySum = CandidateUtils.round(similaritySum/ (double) karyotypeCandidates.size(), 5);

    // not using the list of candidates at the moment. I could integrate into the fitness score a population level score rather than leaving that to the termination criteria.  Have to think about how as it would perhaps simplify the code somewhat.
    //return getBreakpointScore(karyotypeCandidate) + getAneuploidyScore(karyotypeCandidate) + similaritySum;
    return getBreakpointScore(karyotypeCandidate) + similaritySum;
    }

  // Higher scores are naturally fit for watchmaker;
  @Override
  public boolean isNatural()
    {
    return naturalFitness;
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

  }
