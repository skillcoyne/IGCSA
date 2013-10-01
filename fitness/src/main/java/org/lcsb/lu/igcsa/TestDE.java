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
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.watchmaker.*;
import org.lcsb.lu.igcsa.watchmaker.bp.BandCandidateFactory;
import org.lcsb.lu.igcsa.watchmaker.bp.BandSetEvaluator;
import org.lcsb.lu.igcsa.watchmaker.bp.DECrossover;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import java.util.*;

public class TestDE
  {
  static Logger log = Logger.getLogger(TestDE.class.getName());

  public static void main(String[] args) throws Exception
    {
    ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});
    KaryotypeDAO dao = (KaryotypeDAO) context.getBean("karyotypeDAO");

    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();

    // creates the initial population
    CandidateFactory<Set<Band>> candidateFactory = new BandCandidateFactory(bandProbability, new PoissonDistribution(5));

    List<Set<Band>> population = new ArrayList<Set<Band>>(candidateFactory.generateInitialPopulation(50, new Random()));

    BandSetEvaluator evaluator = new BandSetEvaluator(bandProbability, dao.getGeneralKarytoypeDAO().getProbabilityClass("aberration"));

    DECrossover crossover = new DECrossover(0.9, 0.7, evaluator);
    //DEMutator mutator = new DEMutator(0.8, candidateFactory);


    // g generations
    for (int g = 0; g < 100; g++)
      {

      for (int p = 0; p < population.size(); p++)
        {
        //List<Set<Band>> samples =  crossoverSelection(population, 3);

        List<Set<Band>> co = crossover.apply(population, new Random());

        //log.info(co);

        //System.exit(1);

        }


      log.info(g);
      }


    }


  private static List<Set<Band>> crossoverSelection(List<Set<Band>> population, int select)
    {
    Collections.shuffle(population, new MersenneTwisterRNG());
    List<Set<Band>> selected = new ArrayList<Set<Band>>(population.subList(0, select));
    return selected;
    }

  }
