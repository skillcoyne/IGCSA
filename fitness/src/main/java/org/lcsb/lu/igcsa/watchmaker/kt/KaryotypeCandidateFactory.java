/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import java.util.*;

public class KaryotypeCandidateFactory extends AbstractCandidateFactory<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(KaryotypeCandidateFactory.class.getName());

  private KaryotypeDAO dao;
  private IntegerDistribution breakpointDist;

  public KaryotypeCandidateFactory(KaryotypeDAO dao, IntegerDistribution breakpointDist)
    {
    this.dao = dao;
    this.breakpointDist = breakpointDist;
    }

  @Override
  public KaryotypeCandidate generateRandomCandidate(Random rng)
    {
    KaryotypeCandidate candidate = new KaryotypeCandidate();

    try
      {
      generateBreakpoints(candidate);
      generateAneuploidy(candidate);
      }
    catch (ProbabilityException e)
      {
      throw new RuntimeException(e);
      }

    return candidate;
    }


  @Override
  public List<KaryotypeCandidate> generateInitialPopulation(int populationSize, Collection<KaryotypeCandidate> seedCandidates, Random rng)
    {
    log.info("******* generateInitialPopulation **********");
    List<KaryotypeCandidate> population = super.generateInitialPopulation(populationSize, seedCandidates, rng);

    for(KaryotypeCandidate cand: population)
      CandidateGraph.updateGraph(cand, population);

    return population;
    }

  private void generateAneuploidy(KaryotypeCandidate candidate) throws ProbabilityException
    {
    int totalPloidy = selectCount("aneuploidy");

    for (int i = 1; i <= totalPloidy; i++)
      {
      String chromosome = (String) dao.getAneuploidyDAO().getChromosomeProbabilities().roll();
      String ploidy = (String) dao.getAneuploidyDAO().getGainLoss(chromosome).roll();
      if (ploidy.equals("gain"))
        candidate.gainChromosome(chromosome);
      else
        candidate.loseChromosome(chromosome);
      }
    }

  private void generateBreakpoints(KaryotypeCandidate candidate) throws ProbabilityException
    {
    // I always want at least 1 breakpoint
    int maxBands = breakpointDist.sample();
    while (maxBands <= 0)
      maxBands = breakpointDist.sample();

    for (int i = 0; i < maxBands; i++)
      candidate.addBreakpoint((Band) dao.getGeneralKarytoypeDAO().getOverallBandProbabilities().roll());
    }


  private int selectCount(String abtype) throws ProbabilityException
    {
    IntRange abrRange = (IntRange) dao.getGeneralKarytoypeDAO().getProbabilityClass(abtype).roll();
    return new RandomRange(abrRange).nextInt();
    }

  }
