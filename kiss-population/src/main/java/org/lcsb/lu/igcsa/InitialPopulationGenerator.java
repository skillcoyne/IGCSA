/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.*;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.BreakpointCondition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class InitialPopulationGenerator
  {
  static Logger log = Logger.getLogger(InitialPopulationGenerator.class.getName());

  static ApplicationContext context;

  public InitialPopulationGenerator()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});
    }


  private void run()
    {
    KaryotypeDAO dao = (KaryotypeDAO) context.getBean("karyotypeDAO");


    Probability aneuploidyProb = dao.getAneuploidyDAO().getChromosomeProbabilities();
    Probability ploidyCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aneuploidy");
    Probability bpCountProb = dao.getGeneralKarytoypeDAO().getProbab  ilityClass("aberration"); // not really used right now
    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();


    Collection<Band> allPossibleBands = new ArrayList<Band>();
    for (Object b : bandProbability.getRawProbabilities().keySet())
      allPossibleBands.add((Band) b);
    BreakpointWatcher.getWatcher().setExpectedBreakpoints(allPossibleBands);

    CandidateFactory<KaryotypeCandidate> factory = new KaryotypeCandidateFactory(dao, new PoissonDistribution(5), false);

    Evaluator evaluator = new Evaluator(bandProbability, bpCountProb, aneuploidyProb, ploidyCountProb, false);

    List<EvolutionaryOperator<KaryotypeCandidate>> operators = new LinkedList<EvolutionaryOperator<KaryotypeCandidate>>();
    operators.add(new Crossover(0.7, 0.9, evaluator));
    operators.add(new Mutator(0.2, 0.05, factory, evaluator));


    int maxPop = 300;
    EvolutionEngine<KaryotypeCandidate> engine = new DiversityEvolution<KaryotypeCandidate>(
        factory, evaluator,
        new EvolutionPipeline<KaryotypeCandidate>(operators),
        new KaryotypeSelectionStrategy(1.5, 0.2),
        new MersenneTwisterRNG(), maxPop);

    Observer observer = new Observer();
    engine.addEvolutionObserver(observer);

    List<EvaluatedCandidate<KaryotypeCandidate>> pop = engine.evolvePopulation(maxPop, 0,
        new BreakpointCondition(allPossibleBands, 2),
        //new PopulationConditions( new BreakpointRepresentation(2.0) )
        new GenerationCount(1000)
    );

    }


  }
