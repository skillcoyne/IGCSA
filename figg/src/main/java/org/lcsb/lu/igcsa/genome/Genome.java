package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.concurrency.Mutable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.tables
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class Genome
  {
  static Logger log = Logger.getLogger(Genome.class.getName());

  protected String buildName;
  protected File genomeDirectory;
  protected File mutationDirectory;

  protected HashMap<String, Chromosome> chromosomes = new HashMap<String, Chromosome>();


  public void setBuildName(String buildName)
    {
    this.buildName = buildName.replaceAll(" ", "_");
    }

  public String getBuildName()
    {
    return this.buildName;
    }

  public File getGenomeDirectory()
    {
    return this.genomeDirectory;
    }

  public void setGenomeDirectory(File genomeDirectory)
    {
    this.genomeDirectory = genomeDirectory;
    }

  public File getMutationDirectory()
    {
    return this.mutationDirectory;
    }

  public void setMutationDirectory(File mutationDir)
    {
    this.mutationDirectory = mutationDir;
    }

  public void addChromosomes(Chromosome[] chromosomes)
    {
    for (Chromosome chr : chromosomes)
      this.chromosomes.put(chr.getName(), chr);
    }

  public void addChromosome(Chromosome chromosome)
    {
    this.chromosomes.put(chromosome.getName(), chromosome);
    }

  public void removeChromosome(String name)
    {
    this.chromosomes.remove(name);
    }

  public void replaceChromosome(Chromosome chr)
    {
    if (chromosomes.containsKey(chr.getName())) this.addChromosome(chr);
    else log.warn("No chromosome " + chr.getName() + " to be replaces. Added instead.");
    }

  public boolean hasChromosome(String name)
    {
    return this.chromosomes.containsKey(name);
    }

  public Chromosome[] getChromosomes()
    {
    return this.chromosomes.values().toArray(new Chromosome[chromosomes.values().size()]);
    }

  public Chromosome getChromosome(String name)
    {
    return this.chromosomes.get(name);
    }

  public abstract Genome copy();
  }
