package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

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

  /**
   * Exactly what it sounds like.
   * @return
   */
  public Chromosome[] getChromosomes();

  /**
   * Add variation with corresponding probabilities.
   * @param variation
   * @param probabilityList
   */
  public void addVariationType(Variation variation, ProbabilityList probabilityList);

  /**
   * @return Map with key-value pairs sorted by the frequency of the ProbabilityLists
   */
  public Map<Variation, ProbabilityList> getVariations();

  /**
   * Not recommended for general use unless you have very small chromosomes as this holds the entire
   * genome in memory. Better use is to call #mutate(Chromosome chr, int window) and output the new
   * chromosome.
   * @param window
   */
  public abstract void mutate(int window);

  /**
   *
   * @param chromosome
   * @param window
   * @return Chromosome object with entire new sequence.  Might be more efficient to just keep the mutated
   * sequences with locations but...
   */
  public abstract Chromosome mutate(Chromosome chromosome, int window);

  public abstract void mutate(Chromosome chr, int window, FASTAWriter writer) throws IOException;
  }
