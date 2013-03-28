package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.IOException;
import java.util.List;

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

  public void setVariantTypes(List<Variation> variantTypes);

  public void addChromosomes(Chromosome[] chromosomes);

  public void addChromosome(Chromosome chromosome);

  public boolean hasChromosome(String name);

//  public void setMutationWriter(MutationWriter writer);


  /**
   * Exactly what it sounds like.
   * @return
   */
  public Chromosome[] getChromosomes();


  /**
   * Mutate the entire genome at once and output. This method just loops through the chromosomes and calls
   * Genome#mutate(chromosome, window, writer)
   * @param window
   */
  public abstract Genome mutate(int window, FASTAWriter writer);

  public abstract Mutable mutate(Chromosome chr, int window, FASTAWriter writer);
  //public abstract Chromosome mutate(Chromosome chr, int window, FASTAWriter writer) throws IOException;
  }
