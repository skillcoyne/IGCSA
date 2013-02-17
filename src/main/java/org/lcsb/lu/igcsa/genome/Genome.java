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


  /* TODO Not really sure how this should work in the actual code yet. */
  // one thing, each chromosome can be mutated concurrently in the reference genome as theere will
  // not be any translocations.  I don't actually think I know how to do that...
  //public abstract void mutateChromosome(String chr, Location loc);


  }
