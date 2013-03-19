package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface Genome
  {
  public void setBuildName(String buildName);
  public String getBuildName();

  public List<Variation> getVariantTypes();

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
   * Mutate the entire genome at once and output. This method just loops through the chromosomes and calls
   * Genome#mutate(chromosome, window, writer)
   * @param window
   */
  public abstract Genome mutate(int window, FASTAWriter writer);

  /**
   *
   * @param chromosome
   * @param window
   * @return Chromosome object with entire new sequence.  Might be more efficient to just keep the mutated
   * sequences with locations but...
   */
  public abstract Chromosome mutate(Chromosome chromosome, int window);

  public abstract Chromosome mutate(Chromosome chr, int window, FASTAWriter writer) throws IOException;
  }
