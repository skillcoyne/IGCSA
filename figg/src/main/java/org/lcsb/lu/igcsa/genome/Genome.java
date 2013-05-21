package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.concurrency.Mutable;

import java.io.File;

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

  public File getGenomeDirectory();
  public void setGenomeDirectory(File genomeDirectory);

  public File getMutationDirectory();
  public void setMutationDirectory(File mutationDir);

  public void addChromosomes(Chromosome[] chromosomes);

  public void addChromosome(Chromosome chromosome);

  public void replaceChromosome(Chromosome chromosome);

  public boolean hasChromosome(String name);

  /**
   * Exactly what it sounds like.
   * @return
   */
  public Chromosome[] getChromosomes();

  public Chromosome getChromosome(String name);

  public abstract Mutable mutate(Chromosome chr, int window, FASTAWriter writer);

  }
