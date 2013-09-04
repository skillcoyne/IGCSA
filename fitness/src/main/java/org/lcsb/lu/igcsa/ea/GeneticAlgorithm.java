/**
 * org.lcsb.lu.igcsa.ea
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.ea;

import org.apache.log4j.Logger;

public interface GeneticAlgorithm
  {

  // GeneticAlgorithm(int crossOverRate, int mutationRate, PopulationGenerator populationGenerator );

  public Population evolve(Population initialPopulation, StopCondition stopCondition);





  }
