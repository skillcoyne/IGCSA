/**
 * org.lcsb.lu.igcsa.ea
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.watchmaker.bp.*;
import org.lcsb.lu.igcsa.watchmaker.kt.*;
import org.lcsb.lu.igcsa.watchmaker.kt.Observer;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.BreakpointCondition;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.MinimumConditions;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.MinimumFitness;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.util.*;

public class TestWM
  {
  static Logger log = Logger.getLogger(TestWM.class.getName());


  public static void main(String[] args) throws Exception
    {
    ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});
    KaryotypeDAO dao = (KaryotypeDAO) context.getBean("karyotypeDAO");


    Probability aneuploidyProb = dao.getAneuploidyDAO().getChromosomeProbabilities();
    Probability ploidyCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aneuploidy");
    Probability bpCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aberration"); // not really used right now
    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();

    Collection<Band> allPossibleBands = new ArrayList<Band>();
    for (Object b: bandProbability.getRawProbabilities().keySet())
      allPossibleBands.add((Band)b);
    BreakpointWatcher.getInstance().addAll(allPossibleBands);

    CandidateFactory<KaryotypeCandidate> factory = new KaryotypeCandidateFactory(dao, new PoissonDistribution(5));

    Fitness evaluator = new Fitness(bandProbability, bpCountProb, aneuploidyProb, ploidyCountProb, false);

    List<EvolutionaryOperator<KaryotypeCandidate>> operators = new LinkedList<EvolutionaryOperator<KaryotypeCandidate>>();
    operators.add( new Crossover(0.9, 0.7, evaluator) );
    operators.add( new Mutator(0.2, 0.05, factory, evaluator) );

    SelectionStrategy selection = new KaryotypeSelectionStrategy(2.1);
    //SelectionStrategy selection = new TruncationSelection(0.5);
    //SelectionStrategy selection = new TournamentSelection(new org.uncommons.maths.random.Probability(0.6));
    //SelectionStrategy selection = new SigmaScaling();


//    EvolutionEngine<KaryotypeCandidate> engine = new GenerationalEvolutionEngine<KaryotypeCandidate>(factory, new EvolutionPipeline<KaryotypeCandidate>(operators), evaluator, selection, new MersenneTwisterRNG());

    int maxPop = 200;
    EvolutionEngine<KaryotypeCandidate> engine = new DiversityEvolution<KaryotypeCandidate>(factory, evaluator, new EvolutionPipeline<KaryotypeCandidate>(operators), selection, new MersenneTwisterRNG(), maxPop);

    Observer observer = new Observer();
    engine.addEvolutionObserver(observer);

    TerminationCondition bpTerm = new BreakpointCondition(8.0, 5);
//    TerminationCondition minFitness = new MinimumFitness(0.7);
//    TerminationCondition minCond = new MinimumConditions(9.0, 2.0);
    TerminationCondition generations = new GenerationCount(1000);

    List<EvaluatedCandidate<KaryotypeCandidate>> pop = engine.evolvePopulation(maxPop, 0, generations, bpTerm);

    StringBuffer buff = new StringBuffer("Min\tMax\tMean\tSizeSD\n");
    for (int i=0; i<observer.getMinFitness().size(); i++)
      {
      buff.append(
          observer.getMinFitness().get(i) + "\t" + observer.getMaxFitness().get(i) + "\t" + observer.getMeanFitness().get(i) + "\t" + observer.getSizeStdDev().get(i) + "\n");
      }

    log.info("Total generations: " + observer.getMinFitness().size() + " Population size: " + pop.size() + "\n");

    CandidateGraph cg =  CandidateGraph.getInstance();
    Iterator<DefaultWeightedEdge> eI = cg.weightSortedEdgeIterator();

    DataSet weight = new DataSet(cg.edgeCount());
    while (eI.hasNext())
      weight.addValue(cg.getEdgeWeight(eI.next()));

    log.info("Min:" + weight.getMinimum() + " Max:" + weight.getMaximum() + " Mean:" + weight.getArithmeticMean() + " SD:" + weight.getStandardDeviation());

    log.info( (weight.getMaximum() - weight.getMinimum())/4 );


    log.info("Satisfied conditions: ");
    for (TerminationCondition tc: engine.getSatisfiedTerminationConditions())
      log.info(tc);

    for(EvaluatedCandidate<KaryotypeCandidate> candidate: pop)
      log.info(candidate.getFitness() + " " + candidate.getCandidate());


    }

  }
