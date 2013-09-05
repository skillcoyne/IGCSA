/**
 * org.lcsb.lu.igcsa.ea
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.ea;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.Range;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.prob.Probability;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.interactive.InteractiveSelection;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RankSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.TargetFitness;

import java.util.*;

public class TestWM
  {
  static Logger log = Logger.getLogger(TestWM.class.getName());


  public static void main(String[] args) throws Exception
    {
    ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});
    KaryotypeDAO dao = (KaryotypeDAO) context.getBean("karyotypeDAO");

    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();

    // creates the initial population
    CandidateFactory<Set<Band>> candidateFactory = new BandCandidateFactory(bandProbability, new PoissonDistribution(5));

    List<EvolutionaryOperator<Set<Band>>> operators = new LinkedList<EvolutionaryOperator<Set<Band>>>();
    operators.add(new DECrossoverOperator(0.9, 0.7, new BandSetEvaluator(bandProbability)));
    operators.add(new DEMutator(0.8, candidateFactory));

    EvolutionaryOperator<Set<Band>> operatorPipeline = new EvolutionPipeline<Set<Band>>(operators);

    /* --- ENGINE --- */
    EvolutionEngine<Set<Band>> engine = new GenerationalEvolutionEngine<Set<Band>>(
        candidateFactory,
        operatorPipeline,
        new BandSetEvaluator(bandProbability),
        new DESelectionStrategy(),
        new MersenneTwisterRNG() );


    engine.addEvolutionObserver( new EvolutionObserver<Set<Band>>()
    {
    @Override
    public void populationUpdate(PopulationData<? extends Set<Band>> populationData)
      {
      log.info("Generation: " + populationData.getGenerationNumber());
      log.info("\t Population size: " + populationData.getPopulationSize());
      log.info("\t Mean fitness: " + populationData.getMeanFitness());
      //log.info("\t" + populationData.getBestCandidate());
      }
    });

    List<EvaluatedCandidate<Set<Band>>> pop = engine.evolvePopulation(10, 0, new GenerationCount(10));

    for(EvaluatedCandidate<Set<Band>> c: pop)
      log.info(c.getCandidate());

    // currently far too simplistic, I'm getting a single individual with more and more mutations.  I think I would need a new evolution engine to get around this
    }


  public static class BandCandidateFactory extends AbstractCandidateFactory<Set<Band>>
    {
    private Probability bandProbability;
    private IntegerDistribution distribution;

    public BandCandidateFactory(Probability bandProbability, IntegerDistribution distribution)
      {
      this.distribution = distribution;
      this.bandProbability = bandProbability;
      }

    @Override
    public Set<Band> generateRandomCandidate(Random random)
      {
      int maxBands = distribution.sample();
      Set<Band> bands = new HashSet<Band>(maxBands);
      for (int i = 0; i < maxBands; i++)
        bands.add((Band) bandProbability.roll());

      return bands;
      }
    }

  public static class DECrossoverOperator implements EvolutionaryOperator<Set<Band>>
    {
    private double F = 0.5; // between 0,2
    private double CR = 0.5;

    private FitnessEvaluator<Set<Band>> fitness;

    private final Range FLIMIT = new DoubleRange(0, 2);
    private final Range CRLIMIT = new DoubleRange(0, 1);

    public DECrossoverOperator(double F, double crossoverRate, FitnessEvaluator<Set<Band>> fitness)
      {
      if (!FLIMIT.containsDouble(F) || !CRLIMIT.containsDouble(crossoverRate))
        throw new IllegalArgumentException("F must be between 0,2 and CR must be between 0,1");

      this.F = F;
      this.CR = crossoverRate;
      this.fitness = fitness;
      }

    @Override
    public List<Set<Band>> apply(List<Set<Band>> sets, Random random)
      {
//      if (sets.size() != 3)
//        throw new IllegalArgumentException("Three individuals required for crossover");

      Set<Band> xr1 = sets.get(0);
      Set<Band> xr2 = sets.get(1);
      Set<Band> xr3 = sets.get(2);

      Set<Band> trial = sets.get(2);

      // I'm only going to care about bands that show up in one of my sets
      List<Band> all = new ArrayList<Band>(xr1);
      all.addAll(xr2);
      all.addAll(xr3);

      //randomly select first trial
      Collections.shuffle(all, new MersenneTwisterRNG());
      Iterator<Band> bI = all.iterator();
      while (bI.hasNext())
        {
        Band b = bI.next();

        if (new Random().nextDouble() < CR) //
          {
          int x1 = (xr1.contains(b)) ? 1 : 0;
          int x2 = (xr2.contains(b)) ? 1 : 0;
          int x3 = (xr3.contains(b)) ? 1 : 0;

          //  v1 is generated by adding the differences of xr1 and xr2 with a weighting factor F (0,2)
          double v1 = x3 + F * (x1 - x2);

          if (v1 > 0)
            trial.add(b);
          if (v1 < 0 && trial.contains(b))
            trial.remove(b);
          }

        bI.remove();
        Collections.shuffle(all, new MersenneTwisterRNG()); // "noisy random vector"
        }

      // supposed to evaluate fitness and replace an individual if you were looping through the population one at a time.  So I'll just check/replace the last one
      if (fitness.getFitness(trial, sets) < fitness.getFitness(sets.get(sets.size()-1), sets))
        {
        sets.remove(sets.size()-1);
        sets.add(trial);
        }

      return sets;
      }
    }

  public static class DEMutator implements EvolutionaryOperator<Set<Band>>
    {
    private double MR = 0.3;

    private final Range MRLIMIT = new DoubleRange(0, 1);
    private CandidateFactory<Set<Band>> factory;


    public DEMutator(double MR, CandidateFactory<Set<Band>> factory)
      {
      if (!MRLIMIT.containsDouble(MR))
        throw new IllegalArgumentException("MR must be between 0,1");

      this.MR = MR;
      this.factory = factory;
      }

    @Override
    public List<Set<Band>> apply(List<Set<Band>> candidates, Random random)
      {
      // either every individual can generate a new set?  Or every individual uses the same set. For now we'll go with 2.
      Set<Band> bands = factory.generateRandomCandidate(new Random()); // select breakponits to flip
      Iterator<Set<Band>> cI = candidates.iterator();
      while (cI.hasNext())
        {
        Set<Band> ind = cI.next();
        for (Band b : bands)
          {
          if (random.nextDouble() < MR)
            { // either add or remove it from the set
            if (ind.contains(b))
              ind.remove(b);
            else
              ind.add(b);
            }
          }
        }
      return candidates;
      }
    }

  public static class BandSetEvaluator implements FitnessEvaluator<Set<Band>>
    {
    private Map<Band, Double> probabilities = new HashMap<Band, Double>();

    public BandSetEvaluator(Probability bandProbability)
      {
      for (Map.Entry<Double, Object> entry : bandProbability.getProbabilities().entrySet())
        probabilities.put((Band) entry.getValue(), entry.getKey());
      }

    @Override
    public double getFitness(Set<Band> bands, List<? extends Set<Band>> sets)
      {
      double score = 0.0;
      for (Band b : bands)
        score += probabilities.get(b);

      // while technically a genome with no mutations is perfectly fit that is not what I'm looking for
      if (score == 0)
        score = 100;

      return score;
      }

    @Override
    public boolean isNatural()  // in my case higher fitness values actually means lower fitness
    {
    return false;
    }
    }


  public static class DESelectionStrategy implements SelectionStrategy<Object>
    {

    @Override
    public <S> List<S> select(List<EvaluatedCandidate<S>> population, boolean naturalFitnessScores, int selectionSize, Random rng)
      {
      // this will be an odd use of the engine I think. But I'll just shuffle the population and throw them all back up.  The CrossOver operator will alter it
      Collections.shuffle(population, new MersenneTwisterRNG());

      List<S> selected = new ArrayList<S>();
      for(EvaluatedCandidate<S> c: population)
        selected.add(c.getCandidate());

      return selected;
      }
    }



  }
