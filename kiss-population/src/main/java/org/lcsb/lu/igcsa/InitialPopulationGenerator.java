/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.AberrationRules;
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.watchmaker.kt.*;
import org.lcsb.lu.igcsa.watchmaker.kt.Observer;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.BreakpointCondition;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.termination.GenerationCount;


import java.util.*;

public class InitialPopulationGenerator
  {
  static Logger log = Logger.getLogger(InitialPopulationGenerator.class.getName());

  static ApplicationContext context;

  public static void main(String[] args) throws Exception
    {
    new InitialPopulationGenerator().run();
    }

  public InitialPopulationGenerator()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});
    }

  public void run() throws ProbabilityException
    {
    KaryotypeDAO dao = (KaryotypeDAO) context.getBean("karyotypeDAO");

    Probability aneuploidyProb = dao.getAneuploidyDAO().getChromosomeProbabilities();
    Probability ploidyCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aneuploidy");
    Probability bpCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aberration"); // not really used right now
    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();

    Collection<Band> allPossibleBands = new ArrayList<Band>();
    for (Object b : bandProbability.getRawProbabilities().keySet())
      allPossibleBands.add((Band) b);

    CandidateFactory<KaryotypeCandidate> factory = new KaryotypeCandidateFactory(dao, new PoissonDistribution(5), true);

    Evaluator evaluator = new Evaluator(bandProbability, bpCountProb, aneuploidyProb, ploidyCountProb, false);
    List<EvolutionaryOperator<KaryotypeCandidate>> operators = new LinkedList<EvolutionaryOperator<KaryotypeCandidate>>();
    operators.add(new Crossover(0.7, 0.9, evaluator));
    operators.add(new Mutator(0.2, 0.05, factory, evaluator));

    int maxPop = 200;
    EvolutionEngine<KaryotypeCandidate> engine = new DiversityEvolution<KaryotypeCandidate>(
        factory,
        evaluator,
        new EvolutionPipeline<KaryotypeCandidate>(operators),
        new KaryotypeSelectionStrategy(2.0, 0.2),
        new MersenneTwisterRNG(),
        maxPop);

    Observer observer = new Observer();
    engine.addEvolutionObserver(observer);

    List<EvaluatedCandidate<KaryotypeCandidate>> pop = engine.evolvePopulation(
        maxPop,
        0,
        new BreakpointCondition(allPossibleBands, 2),
        //new PopulationConditions( new BreakpointRepresentation(2.0) )
        new GenerationCount(1000));

    for (EvaluatedCandidate<KaryotypeCandidate> candidate : pop)
      {
      log.info(candidate.getFitness() + " " + candidate.getCandidate());

       createKaryotype(candidate.getCandidate()) ;
      }


    }


  private List<Aberration> createKaryotype(KaryotypeCandidate candidate)
    {
    Collection<Band> bps = candidate.getBreakpoints();

    AberrationRules rules = new AberrationRules();
    rules.applyRules(bps.toArray(new Band[bps.size()]));

    List<List<Band>> orderedBands = new ArrayList<List<Band>>(rules.getOrderedBreakpointSets().keySet());

    Map<Band, List<List<Band>>> bandAbrMap = new HashMap<Band, List<List<Band>>>();
    for (Band band : bps)
      {
      if (!bandAbrMap.containsKey(band))
        bandAbrMap.put(band, new ArrayList<List<Band>>());

      for (List<Band> orderedBandList : orderedBands)
        {
        if (orderedBandList.contains(band))
          bandAbrMap.get(band).add(orderedBandList);
        }
      }

    Set<Band> used = new HashSet<Band>();
    List<Aberration> abrList = new ArrayList<Aberration>();
    for (Map.Entry<Band, List<List<Band>>> entry : bandAbrMap.entrySet())
      {
      Band currentBand = entry.getKey();
      if (used.contains(currentBand))
        continue;

      Collections.shuffle(entry.getValue());
      List<Band> bandList = entry.getValue().get(0);

      // only use any given band once
      boolean seen = false;
      for (Band b : bandList)
        if (used.contains(b))
          seen = true;
      if (seen)
        continue;

      used.addAll(bandList);

      List<ICombinatoricsVector<Aberration>> abrPerBand = rules.getOrderedBreakpointSets().get(bandList);
      Aberration abr = abrPerBand.get(new Random().nextInt(abrPerBand.size())).getVector().get(0);
      abrList.add(abr);
      }

    // TODO collect aneuploidies & aberrations, put into HBase tables.  Write extractor job that will assemble a karyotype from the HBase tables

    log.info(candidate.getAneuploidies());
    log.info(abrList);


    return abrList;
    }


  }
