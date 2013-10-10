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

    SelectionStrategy selection = new KaryotypeSelectionStrategy(200, 0.0);
    //SelectionStrategy selection = new TruncationSelection(0.5);
    //SelectionStrategy selection = new TournamentSelection(new org.uncommons.maths.random.Probability(0.6));
    //SelectionStrategy selection = new SigmaScaling();


//    EvolutionEngine<KaryotypeCandidate> engine = new GenerationalEvolutionEngine<KaryotypeCandidate>(factory, new EvolutionPipeline<KaryotypeCandidate>(operators), evaluator, selection, new MersenneTwisterRNG());

    EvolutionEngine<KaryotypeCandidate> engine = new DiversityEvolution<KaryotypeCandidate>(factory, evaluator, new EvolutionPipeline<KaryotypeCandidate>(operators), selection, new MersenneTwisterRNG());

    Observer observer = new Observer();
    engine.addEvolutionObserver(observer);

    TerminationCondition bpTerm = new BreakpointCondition(8.0, 5);
//    TerminationCondition minFitness = new MinimumFitness(0.7);
//    TerminationCondition minCond = new MinimumConditions(9.0, 2.0);
    TerminationCondition generations = new GenerationCount(1000);

    List<EvaluatedCandidate<KaryotypeCandidate>> pop = engine.evolvePopulation(300, 0, generations, bpTerm);

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

  private void testBPOnly(KaryotypeDAO dao) throws ProbabilityException
    {
    Probability abrCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aberration"); // not really used right now
    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();


    /* This was the initial test with Sets of breakpoints only.  No aneuploidy. */
    // creates the initial population
    CandidateFactory<Set<Band>> candidateFactory = new BandCandidateFactory(bandProbability, new PoissonDistribution(5));

    List<EvolutionaryOperator<Set<Band>>> operators = new LinkedList<EvolutionaryOperator<Set<Band>>>();
    operators.add(new DECrossover(0.9, 0.7, new BandSetEvaluator(bandProbability, abrCountProb)));
    operators.add(new DEMutator(0.3, 0.05, candidateFactory));

    /* --- ENGINE --- */
    EvolutionEngine<Set<Band>> engine = new GenerationalEvolutionEngine<Set<Band>>(candidateFactory, new EvolutionPipeline<Set<Band>>(operators), new BandSetEvaluator(bandProbability, abrCountProb), new DESelectionStrategy(), new MersenneTwisterRNG());


    DEObserver<Set<Band>> observer = new DEObserver<Set<Band>>();
    engine.addEvolutionObserver(observer);

    // Terminate when the spread of bands has a stddev of 8.0 and the minimum fitness is above 1.3
    // these are currently just selected by observation. The size SD allows for a good spread of individuals with few, moderate and lots of breakpoints
    // The minimum fitness allows me to effectively ensure that individuals with few or very low fitness breakpoints get represented
    // Currently this tends to terminate after about 600 - 1000 generations (for a population of 200).  Fewer generations for a smaller population
    List<EvaluatedCandidate<Set<Band>>> pop = engine.evolvePopulation(200, 0, new DiversityTermination(0, 8.0, 0.7));

    StringBuffer buff = new StringBuffer("Min\tMax\tMean\tSizeSD\n");
    for (int i=0; i<observer.getMinFitness().size(); i++)
      {
      buff.append(
          observer.getMinFitness().get(i) + "\t" + observer.getMaxFitness().get(i) + "\t" + observer.getMeanFitness().get(i) + "\t" + observer.getSizeStdDev().get(i) + "\n"
      );
      }

    buff.append("Total generations: " + observer.getMinFitness().size() + "\n");

    log.info(buff);

    new DEPopulationEvaluation(pop).outputCurrentStats();

    for (int i = 0; i < pop.size(); i++)
      log.info(i + 1 + ": " + pop.get(i).getCandidate() + " " + pop.get(i).getFitness());


    //    Set<Band> allbands = new HashSet<Band>();
    //    for (Object obj : bandProbability.getRawProbabilities().keySet())
    //      allbands.add((Band) obj);
    //
    //    testBandRepresentation(pop, allbands);

    }





  }
