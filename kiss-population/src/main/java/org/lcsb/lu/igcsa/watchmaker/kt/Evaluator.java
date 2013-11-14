package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultWeightedEdge;
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
public class Evaluator implements FitnessEvaluator<KaryotypeCandidate> // this isn't truely a fitness value
  {
  static Logger log = Logger.getLogger(Evaluator.class.getName());

  private final Probability bpProb;
  private final Probability gainLossProb;
  private final Probability ploidyCountProb;
  private final Probability bpCountProb;

  private boolean naturalFitness = false;

  private Map<Band, Double> breakpointProbs = new HashMap<Band, Double>();

  private double sumProb = 0;

  /*
  Basically this evaluates the liklihood that this candidate occurs based on the probabilities provided.  A higher score means it is less likely to have occurred.
   */

  public Evaluator(Probability bpProb, Probability bpCountProb, Probability gainLossProb, Probability ploidyCountProb, boolean naturalFitness)
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
    score += (double)chromosomes.size()/(double)karyotypeCandidate.getBreakpoints().size();

    // while technically a tables with no mutations is "fit" I'm looking for genomes with at least some mutations
    if (score  == 0) score = 200;

    return score;
    }

  public double getAneuploidyScore(KaryotypeCandidate karyotypeCandidate)
    {
    // it's ok if there are no aneuploidies, so no adjustment for a 0 score.
    double score = 0.0;

    int apCount = 0;
    Set<String> chromosomes = new HashSet<String>();
    for (KaryotypeCandidate.Aneuploidy aneuploidy : karyotypeCandidate.getAneuploidies())
      {
      double prob = gainLossProb.getRawProbabilities().get(aneuploidy.getChromosome());
      score += prob * (aneuploidy.getGain() + aneuploidy.getLoss());

      apCount += Math.abs(aneuploidy.getCount());

      chromosomes.add(aneuploidy.getChromosome());
      }
    // means a gain of 2 counts against the fitness more than a gain of 1
    score += apCount/chromosomes.size();

    return score;
    }


  @Override
  public double getFitness(KaryotypeCandidate karyotypeCandidate, List<? extends KaryotypeCandidate> karyotypeCandidates)
    {
    double similaritySum = 0.0;

    if (karyotypeCandidates.size() > CandidateGraph.getGraph().nodeCount())
      throw new RuntimeException("Graph has not been updated correctly, " + CandidateGraph.getGraph().nodeCount() + " nodes found, " + karyotypeCandidates.size() + " expected");

    Iterator<DefaultWeightedEdge> eI = CandidateGraph.getGraph().getEdges(karyotypeCandidate).iterator();
    while (eI.hasNext())
      similaritySum += CandidateGraph.getGraph().getEdgeWeight(eI.next())/10;

    similaritySum = CandidateUtils.round(similaritySum/ (double) karyotypeCandidates.size(), 5);

    double bpScore = getBreakpointScore(karyotypeCandidate);
    double apScore = (karyotypeCandidate.getAneuploidies().size() > 0)? (getAneuploidyScore(karyotypeCandidate)): 0;

    // not using the list of candidates at the moment. I could integrate into the fitness score a population level score rather than leaving that to the termination criteria.  Have to think about how as it would perhaps simplify the code somewhat.
    return  bpScore + apScore + similaritySum;
//    return getBreakpointScore(karyotypeCandidate) +  similaritySum;
    }

  // Higher scores are naturally fit for watchmaker;
  @Override
  public boolean isNatural()
    {
    return naturalFitness;
    }


  }
