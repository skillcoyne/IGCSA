///**
// * org.lcsb.lu.igcsa.ea
// * Author: sarah.killcoyne
// * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
// * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
// */
//
//
//package org.lcsb.lu.igcsa.watchmaker;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.math3.distribution.PoissonDistribution;
//import org.apache.log4j.Logger;
//import org.lcsb.lu.igcsa.KaryotypeInsilicoGenome;
//import org.lcsb.lu.igcsa.database.Band;
//import org.lcsb.lu.igcsa.database.KaryotypeDAO;
//import org.lcsb.lu.igcsa.generator.Aberration;
//import org.lcsb.lu.igcsa.generator.AberrationRules;
//import org.lcsb.lu.igcsa.genome.Karyotype;
//import org.lcsb.lu.igcsa.prob.Probability;
//import org.lcsb.lu.igcsa.utils.GenerationStatistics;
//import org.lcsb.lu.igcsa.utils.PopulationAneuploidy;
//import org.lcsb.lu.igcsa.watchmaker.kt.*;
//import org.lcsb.lu.igcsa.watchmaker.kt.Observer;
//import org.lcsb.lu.igcsa.watchmaker.kt.termination.BreakpointCondition;
//import org.paukov.combinatorics.ICombinatoricsVector;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//import org.uncommons.maths.random.MersenneTwisterRNG;
//import org.uncommons.maths.statistics.DataSet;
//import org.uncommons.watchmaker.framework.*;
//import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
//import org.uncommons.watchmaker.framework.termination.GenerationCount;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.util.*;
//
//public class TestWM
//  {
//  static Logger log = Logger.getLogger(TestWM.class.getName());
//
//
//  public static void main(String[] args) throws Exception
//    {
//    ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});
//    KaryotypeDAO dao = (KaryotypeDAO) context.getBean("karyotypeDAO");
//
//
//    Probability aneuploidyProb = dao.getAneuploidyDAO().getChromosomeProbabilities();
//    Probability ploidyCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aneuploidy");
//    Probability bpCountProb = dao.getGeneralKarytoypeDAO().getProbab  ilityClass("aberration"); // not really used right now
//    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();
//
//
//    Collection<Band> allPossibleBands = new ArrayList<Band>();
//    for (Object b : bandProbability.getRawProbabilities().keySet())
//      allPossibleBands.add((Band) b);
//    BreakpointWatcher.getWatcher().setExpectedBreakpoints(allPossibleBands);
//
//    CandidateFactory<KaryotypeCandidate> factory = new KaryotypeCandidateFactory(dao, new PoissonDistribution(5), false);
//
//    Evaluator evaluator = new Evaluator(bandProbability, bpCountProb, aneuploidyProb, ploidyCountProb, false);
//
//    List<EvolutionaryOperator<KaryotypeCandidate>> operators = new LinkedList<EvolutionaryOperator<KaryotypeCandidate>>();
//    operators.add(new Crossover(0.7, 0.9, evaluator));
//    operators.add(new Mutator(0.2, 0.05, factory, evaluator));
//
//
//    int maxPop = 300;
//    EvolutionEngine<KaryotypeCandidate> engine = new DiversityEvolution<KaryotypeCandidate>(
//        factory, evaluator,
//        new EvolutionPipeline<KaryotypeCandidate>(operators),
//        new KaryotypeSelectionStrategy(1.5, 0.2),
//        new MersenneTwisterRNG(), maxPop);
//
//    Observer observer = new Observer();
//    engine.addEvolutionObserver(observer);
//
//    List<EvaluatedCandidate<KaryotypeCandidate>> pop = engine.evolvePopulation(maxPop, 0,
//        new BreakpointCondition(allPossibleBands, 2),
//        //new PopulationConditions( new BreakpointRepresentation(2.0) )
//        new GenerationCount(1000)
//    );
//
//
//    /* --------------------------------------------------------------------------------------------------------------------------------------------  */
//
//
//    StringBuffer buff = new StringBuffer("Min\tMax\tMean\tSizeSD\n");
//    for (int i = 0; i < observer.getMinFitness().size(); i++)
//      buff.append(observer.getMinFitness().get(i) + "\t" + observer.getMaxFitness().get(i) + "\t" + observer.getMeanFitness().get(i) + "\t" + observer.getSizeStdDev().get(i) + "\n");
//
//
//    Karyotype karyotype = new KaryotypeInsilicoGenome(context, null).getGenome();
//    for (EvaluatedCandidate<KaryotypeCandidate> candidate : pop)
//      {
//      log.info(candidate.getFitness() + " " + candidate.getCandidate());
//      Karyotype ck = createKaryotype(karyotype, candidate.getCandidate(), context);
//      log.info(ck.toString());
//      }
//
//    PopulationAneuploidy popA = new PopulationAneuploidy(pop);
//    popA.write(new File("/tmp/aneuploidy.txt"));
//
//    BreakpointWatcher.getWatcher().write(new File("/tmp/breakpoints.txt"));
//
//
//    String cond = "";
//    for (TerminationCondition tc : engine.getSatisfiedTerminationConditions())
//      cond = cond + " " + tc.getClass().getSimpleName();
//    log.info("Satisfied conditions: " + cond);
//
//    DataSet weight = CandidateGraph.getEdgeWeightData();
//
//    log.info("Total generations: " + observer.getMinFitness().size() + " Population size: " + pop.size() + "\n");
//    log.info("NCD graph  Min:" + weight.getMinimum() + " Max:" + weight.getMaximum() + " Mean:" + weight.getArithmeticMean() + " SD:" + weight.getStandardDeviation());
//
//    PopulationEvaluation eval = new PopulationEvaluation(pop);
//    eval.outputCurrentStats();
//
//    /* Think these need to be used to plot the generational changes but not sure how yet */
//    File outFile = new File("/tmp/generations.txt");
//    if (!outFile.exists())
//      outFile.createNewFile();
//
//    FileWriter fileWriter = new FileWriter(outFile.getAbsoluteFile());
//    fileWriter.write( "cpxMean\tcpxSD\tbpRepMean\tbpRepSD\tfitnessMean\tbpSizeMean\tbpSizeSD\n" );
//
////    GenerationStatistics tk = GenerationStatistics.getTracker();
////    for (int i=0;i<tk.getComplexityMeans().size(); i++)
////      fileWriter.write(StringUtils.join(new Object[]{
////          tk.getComplexityMeans().get(i),
////          tk.getComplexitySD().get(i),
////          tk.getBpRepresentation().get(i),
////          tk.getBpSD().get(i),
////          tk.getFitnessMeans().get(i),
////          tk.getBpSizeMean().get(i),
////          tk.getBpSizeSD().get(i)
////      }, '\t') + "\n" );
//
//    fileWriter.flush();
//    fileWriter.close();
//
////    log.info(GenerationStatistics.getTracker().getComplexityMeans());
////    log.info(GenerationStatistics.getTracker().getComplexitySD());
////
////    log.info(GenerationStatistics.getTracker().getBpRepresentation());
////    log.info(GenerationStatistics.getTracker().getBpSD());
//
//    }
//
//  private static void outputBreakpoints(List<EvaluatedCandidate<KaryotypeCandidate>> population)
//    {
//    Map<Band, Integer> counts = new HashMap<Band, Integer>();
//
//    for (EvaluatedCandidate<KaryotypeCandidate> ind : population)
//      {
//      for (Band b : ind.getCandidate().getBreakpoints())
//        {
//        if (!counts.containsKey(b))
//          counts.put(b, 0);
//
//        counts.put(b, counts.get(b) + 1);
//        }
//      }
//
//    for (Band b : counts.keySet())
//      System.out.println(b + " " + counts.get(b));
//
//    }
//
//  /*
// This just shows how the final candidates would be used to create a Karyotype class which knows how to apply the mutations
// NOTE: The bands need to have their locations defined in order to correctly create karyotype aberrations.
//  */
//  private static Karyotype createKaryotype(Karyotype kt, KaryotypeCandidate candidate, ApplicationContext context) throws Exception
//    {
//    Karyotype karyotype = kt.copy();
//    karyotype.resetChromosomeCount(2);
//
//    Collection<Band> bps = candidate.getBreakpoints();
//
//    AberrationRules rules = new AberrationRules();
//    rules.applyRules(bps.toArray(new Band[bps.size()]));
//
//    List<List<Band>> orderedBands = new ArrayList<List<Band>>(rules.getOrderedBreakpointSets().keySet());
//
//    Map<Band, List<List<Band>>> bandAbrMap = new HashMap<Band, List<List<Band>>>();
//    for (Band band : bps)
//      {
//      if (!bandAbrMap.containsKey(band))
//        bandAbrMap.put(band, new ArrayList<List<Band>>());
//
//      for (List<Band> orderedBandList : orderedBands)
//        {
//        if (orderedBandList.contains(band))
//          bandAbrMap.get(band).add(orderedBandList);
//        }
//      }
//
//    Set<Band> used = new HashSet<Band>();
//    List<Aberration> abrList = new ArrayList<Aberration>();
//    for (Map.Entry<Band, List<List<Band>>> entry : bandAbrMap.entrySet())
//      {
//      Band currentBand = entry.getKey();
//      if (used.contains(currentBand))
//        continue;
//
//      Collections.shuffle(entry.getValue());
//      List<Band> bandList = entry.getValue().get(0);
//
//      // only use any given band once
//      boolean seen = false;
//      for (Band b : bandList)
//        if (used.contains(b))
//          seen = true;
//      if (seen)
//        continue;
//
//      used.addAll(bandList);
//
//      List<ICombinatoricsVector<Aberration>> abrPerBand = rules.getOrderedBreakpointSets().get(bandList);
//      Aberration abr = abrPerBand.get(new Random().nextInt(abrPerBand.size())).getVector().get(0);
//      abrList.add(abr);
//
//      karyotype.addAberrationDefintion(abr);
//      }
//
//    // not really correct, the Aneuploidy class contains counts which aren't being used
//    for (KaryotypeCandidate.Aneuploidy ploidy : candidate.getAneuploidies())
//      {
//      if (ploidy.getCount() > 0)
//        karyotype.gainChromosome(ploidy.getChromosome(), ploidy.getGain());
//      else
//        karyotype.loseChromosome(ploidy.getChromosome(), ploidy.getLoss());
//      }
//
//    if (!karyotype.getAberrationDefinitions().containsAll(abrList))
//      throw new RuntimeException("Aberrations don't match");
//
//    return karyotype;
//    }
//
//  }
