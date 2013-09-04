/**
 * org.lcsb.lu.igcsa.ea
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.ea;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Population
  {
  static Logger log = Logger.getLogger(Population.class.getName());

  private List<Individual> individuals;
  private int limit = 50;

  public Population(int populationLimit)
    {
    individuals = new ArrayList<Individual>(populationLimit);
    }


  public Population(List<Individual> individuals)
    {
    this.individuals = individuals;
    limit = individuals.size();
    }

  public Iterator<Individual> iterator()
    {
    return null;
    }

  public int getPopulationSize()
    {
    return 0;
    }

  public int getPopulationLimit()
    {
    return 0;
    }

  public Population nextGeneration()
    {
    return null;
    }

  public void addIndividual(Individual individual) throws MaxCountExceededException
    {
    if (individuals.size() == limit)
      throw new MaxCountExceededException(limit);

    this.individuals.add(individual);
    }

  public List<Individual> getFittestIndividuals(int topX)
    {
    return null;
    }

  public List<Individual> getRandomIndividuals(int num)
    {
    return null;
    }


  }
