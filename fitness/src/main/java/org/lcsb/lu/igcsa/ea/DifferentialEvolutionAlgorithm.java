/**
 * org.lcsb.lu.igcsa.ea
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.ea;

import org.apache.log4j.Logger;

public class DifferentialEvolutionAlgorithm implements GeneticAlgorithm
  {
  static Logger log = Logger.getLogger(DifferentialEvolutionAlgorithm.class.getName());

  private int F = 0;  // amplification factor between 0,2

  private int CR = 0; // crossover rate 0,1

  private int MR = 0; // crossover rate 0,1



  @Override
  public Population evolve(Population initialPopulation, StopCondition stopCondition)
    {
    return null;
    }


  private void checkMax(int rate, int max)
    {
    //if (rate > max)
      //throw new IllegalArgumentException()
    }


  }
