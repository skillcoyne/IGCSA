import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.MinimalKaryotype;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.generator.AberrationRules;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.watchmaker.kt.*;
import org.lcsb.lu.igcsa.watchmaker.kt.Observer;
import org.lcsb.lu.igcsa.watchmaker.kt.termination.BreakpointCondition;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.util.*;


/**
 * PACKAGE_NAME
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SpecialGenerator
  {
  static Logger log = Logger.getLogger(SpecialGenerator.class.getName());
  private static ClassPathXmlApplicationContext context;
  private static KaryotypeDAO dao;

  public static void main(String[] args) throws Exception
    {
    SpecialGenerator sg = new SpecialGenerator();
    sg.run("5", "2");
    sg.run("1", "10");
    sg.run("3", "X", "5");
//    sg.run("1", "3", "5", "X");
    }

  public SpecialGenerator()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml"});
    dao = (KaryotypeDAO) context.getBean("karyotypeDAO");
    }

  public void run(String... chromosomes) throws ProbabilityException
    {
    Map<Band, Double> allPossibleBands = new HashMap<Band, Double>();
    for (String c: chromosomes)
      {
      for (Map.Entry<Object, Double> entry: dao.getGeneralKarytoypeDAO().getBandProbabilities(c).getRawProbabilities().entrySet())
        allPossibleBands.put((Band) entry.getKey(), entry.getValue());
      }

    AberrationRules.SINGLETONS = false;
    AberrationRules.SET_SIZE = chromosomes.length;
    AberrationRules rules = new AberrationRules();
    rules.applyRules(allPossibleBands.keySet().toArray(new Band[allPossibleBands.size()]));

    List<ICombinatoricsVector<Band>> breakpoints = rules.getBreakpointSets();
    for (Iterator<ICombinatoricsVector<Band>> bI = breakpoints.iterator(); bI.hasNext();)
      {
      boolean remove = false;
      ICombinatoricsVector<Band> vector = bI.next();
      if (vector.getSize() < chromosomes.length)
        remove = true;

      String lastChr = "";
      for (Band b: vector.getVector())
        { // get rid of vectors that involve only a single chromosome
        if (b.getChromosomeName().equals(lastChr))
          {
          remove = true;
          break;
          }
        lastChr = b.getChromosomeName();
        }
      if (remove) bI.remove();
      }
    log.info( "Breakpoints: " + breakpoints.size() );

    List<Candidate> candidates = new ArrayList<Candidate>();
    for (Iterator<ICombinatoricsVector<Band>> bI = breakpoints.iterator(); bI.hasNext();)
      {
      Candidate cand = new Candidate();
      for (Band b: bI.next())
        cand.addBreakpoint(b, allPossibleBands.get(b));
      candidates.add(cand);
      }
    Collections.sort(candidates);
    Collections.reverse(candidates);

    for (int i=0; i<candidates.size(); i++)
      {
      log.info(candidates.get(i));
      if (i >= 10) break;
      }

    log.info( "Candidates: " + candidates.size());
    }


  static class Candidate implements Comparable<Candidate>
    {
    private List<Band> bands = new ArrayList<Band>();
    private double probScore = 0.0;

    public void addBreakpoint(Band b, double p)
      {
      bands.add(b);
      probScore += p;
      probScore = (double)Math.round(probScore * 10000) / 10000;
      }

    public List<Band> getBands()
      {
      return bands;
      }

    public double getScore()
      {
      return probScore;
      }

    public String toString()
      {
      return "[" + bands.toString() + ", " + probScore + "]";
      }

    public int compareTo(Candidate candidate)
      {
      return Double.compare(this.getScore(), candidate.getScore());
      }
    }

  }
