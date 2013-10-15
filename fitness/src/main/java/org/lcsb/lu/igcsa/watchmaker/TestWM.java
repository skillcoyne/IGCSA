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
import org.lcsb.lu.igcsa.KaryotypeInsilicoGenome;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.AberrationRules;
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.PopulationAneuploidy;
import org.lcsb.lu.igcsa.watchmaker.kt.*;
import org.lcsb.lu.igcsa.watchmaker.kt.Observer;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.BreakpointCondition;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.MinimumConditions;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.MinimumFitness;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.statistics.DataSet;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.io.File;
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
    for (Object b : bandProbability.getRawProbabilities().keySet())
      allPossibleBands.add((Band) b);
    BreakpointWatcher.getInstance().addAll(allPossibleBands);

    CandidateFactory<KaryotypeCandidate> factory = new KaryotypeCandidateFactory(dao, new PoissonDistribution(5));

    Fitness evaluator = new Fitness(bandProbability, bpCountProb, aneuploidyProb, ploidyCountProb, false);

    List<EvolutionaryOperator<KaryotypeCandidate>> operators = new LinkedList<EvolutionaryOperator<KaryotypeCandidate>>();
    operators.add(new Crossover(0.9, 0.7, evaluator));
    operators.add(new Mutator(0.2, 0.05, factory, evaluator));

    SelectionStrategy selection = new KaryotypeSelectionStrategy(2.1);
    //SelectionStrategy selection = new TruncationSelection(0.5);
    //SelectionStrategy selection = new TournamentSelection(new org.uncommons.maths.random.Probability(0.6));
    //SelectionStrategy selection = new SigmaScaling();


    //    EvolutionEngine<KaryotypeCandidate> engine = new GenerationalEvolutionEngine<KaryotypeCandidate>(factory, new EvolutionPipeline<KaryotypeCandidate>(operators), evaluator, selection, new MersenneTwisterRNG());

    int maxPop = 300;
    EvolutionEngine<KaryotypeCandidate> engine = new DiversityEvolution<KaryotypeCandidate>(factory, evaluator, new EvolutionPipeline<KaryotypeCandidate>(operators), selection, new MersenneTwisterRNG(), maxPop);

    Observer observer = new Observer();
    engine.addEvolutionObserver(observer);

    TerminationCondition bpTerm = new BreakpointCondition(8.0, 5);
    //    TerminationCondition minFitness = new MinimumFitness(0.7);
    //    TerminationCondition minCond = new MinimumConditions(9.0, 2.0);
    TerminationCondition generations = new GenerationCount(1);

    List<EvaluatedCandidate<KaryotypeCandidate>> pop = engine.evolvePopulation(maxPop, 0, generations, bpTerm);

    StringBuffer buff = new StringBuffer("Min\tMax\tMean\tSizeSD\n");
    for (int i = 0; i < observer.getMinFitness().size(); i++)
      {
      buff.append(observer.getMinFitness().get(i) + "\t" + observer.getMaxFitness().get(i) + "\t" + observer.getMeanFitness().get(i) + "\t" + observer.getSizeStdDev().get(i) + "\n");
      }

    log.info("Total generations: " + observer.getMinFitness().size() + " Population size: " + pop.size() + "\n");

    CandidateGraph cg = CandidateGraph.getInstance();
    Iterator<DefaultWeightedEdge> eI = cg.weightSortedEdgeIterator();

    DataSet weight = new DataSet(cg.edgeCount());
    while (eI.hasNext())
      weight.addValue(cg.getEdgeWeight(eI.next()));

    log.info("Min:" + weight.getMinimum() + " Max:" + weight.getMaximum() + " Mean:" + weight.getArithmeticMean() + " SD:" + weight.getStandardDeviation());

    log.info((weight.getMaximum() - weight.getMinimum()) / 4);


    log.info("Satisfied conditions: ");
    for (TerminationCondition tc : engine.getSatisfiedTerminationConditions())
      log.info(tc);

    for (EvaluatedCandidate<KaryotypeCandidate> candidate : pop)
      {
      log.info(candidate.getFitness() + " " + candidate.getCandidate());
      //getKaryotype(candidate.getCandidate(), context);
      }

    PopulationAneuploidy popA = new PopulationAneuploidy(pop);
    popA.write(new File("/tmp/aneuploidy.txt"));



    }

  /*
  This just shows how the final candidates would be used to create a Karyotype class which knows how to apply the mutations
  NOTE: The bands need to have their locations defined in order to correctly create karyotype aberrations.
   */
  private static Karyotype getKaryotype(KaryotypeCandidate candidate, ApplicationContext context) throws Exception
    {
    Karyotype karyotype = new KaryotypeInsilicoGenome(context, null).getGenome();

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

      karyotype.addAberrationDefintion(abr);
      }

    for (KaryotypeCandidate.Aneuploidy ploidy : candidate.getAneuploidies())
      {
      if (ploidy.isGain())
        karyotype.gainChromosome(ploidy.getChromosome());
      else
        karyotype.loseChromosome(ploidy.getChromosome());
      }
    return karyotype;
    }

  }
