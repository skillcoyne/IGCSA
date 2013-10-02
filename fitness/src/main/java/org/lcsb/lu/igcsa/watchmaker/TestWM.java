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
import org.lcsb.lu.igcsa.KaryotypeCandidate;
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
import org.uncommons.watchmaker.framework.selection.SigmaScaling;
import org.uncommons.watchmaker.framework.selection.TournamentSelection;
import org.uncommons.watchmaker.framework.selection.TruncationSelection;
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


    int maxPopulationSize = 200;
    CandidateFactory<KaryotypeCandidate> factory = new KaryotypeCandidateFactory(dao, new PoissonDistribution(5));

    Fitness evaluator = new Fitness(bandProbability, bpCountProb, aneuploidyProb, ploidyCountProb, false);

    List<EvolutionaryOperator<KaryotypeCandidate>> operators = new LinkedList<EvolutionaryOperator<KaryotypeCandidate>>();
    operators.add( new Crossover(0.9, 0.7, evaluator) );
    operators.add( new Mutator(0.3, 0.05, factory));

    //SelectionStrategy selection = new KaryotypeSelectionStrategy(maxPopulationSize);
    //SelectionStrategy selection = new TruncationSelection(0.5);
//    SelectionStrategy selection = new TournamentSelection(new org.uncommons.maths.random.Probability(0.6));
    SelectionStrategy selection = new SigmaScaling();

    EvolutionEngine<KaryotypeCandidate> engine = new GenerationalEvolutionEngine<KaryotypeCandidate>(factory, new EvolutionPipeline<KaryotypeCandidate>(operators), evaluator, selection, new MersenneTwisterRNG());

    Observer observer = new Observer();
    engine.addEvolutionObserver(observer);

    //MinimumConditions termination =  new MinimumConditions(0, 8.0, 0.7);
    TerminationCondition bpTerm = new BreakpointCondition(8.0);
    TerminationCondition minFitness = new MinimumFitness(0.7);

    TerminationCondition minCond = new MinimumConditions(9.0, 2.0);
    TerminationCondition generations = new GenerationCount(3000);

    List<EvaluatedCandidate<KaryotypeCandidate>> pop = engine.evolvePopulation(maxPopulationSize, maxPopulationSize/2, minCond, generations);

    StringBuffer buff = new StringBuffer("Min\tMax\tMean\tSizeSD\n");
    for (int i=0; i<observer.getMinFitness().size(); i++)
      {
      buff.append(
          observer.getMinFitness().get(i) + "\t" + observer.getMaxFitness().get(i) + "\t" + observer.getMeanFitness().get(i) + "\t" + observer.getSizeStdDev().get(i) + "\n"
      );
      }

    buff.append("Total generations: " + observer.getMinFitness().size() + "\n");

    //log.info(buff);

    for (int i = 0; i < pop.size(); i++)
      log.info(i + 1 + ": " + pop.get(i).getCandidate() + " " + pop.get(i).getFitness());


    log.info("Satisfied conditions: ");
    for (TerminationCondition tc: engine.getSatisfiedTerminationConditions())
      log.info(tc);


    //new PopulationEvaluation((List<EvaluatedCandidate<? extends KaryotypeCandidate>>) pop).outputCurrentStats();
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



  private static void testBandRepresentation(List<EvaluatedCandidate<Set<Band>>> population, Set<Band> possibleBands)
    {
    Map<Band, Integer> bandCount = new HashMap<Band, Integer>();
    for (Band b : possibleBands)
      bandCount.put(b, 0);

    for (EvaluatedCandidate<Set<Band>> ind : population)
      {
      for (Band b : ind.getCandidate())
        bandCount.put(b, bandCount.get(b) + 1);
      }

    DataSet stats = new DataSet(possibleBands.size());

    StringBuffer buf = new StringBuffer();
    for (Map.Entry<Band, Integer> entry : bandCount.entrySet())
      stats.addValue(entry.getValue());

    log.info("--- Bands ---");
    log.info("Min:" + stats.getMinimum() + " Max:" + stats.getMaximum() + " Mean:" + stats.getArithmeticMean() + " SD:" + stats.getStandardDeviation());
    /*
    TODO this could get added to the termination conditions. Basically that you don't want to see a band show up in more than 50% of the population (hasn't happened so far).
    Or even that it should show up in at least 1% of the population, ensuring that the band shows up somewhere.
     */

    }

  /*
 For the probabilities that are given in ranges (number of chromosomes that break, number of aneuploidy's)
  */
  private static int selectCount(Probability prob) throws ProbabilityException
    {
    IntRange abrRange = (IntRange) prob.roll();
    return new RandomRange(abrRange).nextInt();
    }


  }
