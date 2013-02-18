package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.Variation;

import java.util.Collection;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface Genome
  {

  public String getBuildName();

  public void addChromosomes(Chromosome[] chromosomes);

  public void addChromosome(Chromosome chromosome);

  public boolean hasChromosome(String name);

  public Chromosome[] getChromosomes();

  public void addVariationType(Variation variation, ProbabilityList probabilityList);

  public abstract Genome mutate(int window);
  }
