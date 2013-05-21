package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.concurrency.Mutable;

import java.io.File;


/**
 * org.lcsb.lu.igcsa.genome
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class CancerGenome implements Genome
  {
  static Logger log = Logger.getLogger(CancerGenome.class.getName());

  private String buildName;

  public void CancerGenome()
    { }

  @Override
  public void setBuildName(String buildName)
    {
    this.buildName = buildName;
    }

  @Override
  public String getBuildName()
    {
    return this.buildName;
    }

  @Override
  public File getGenomeDirectory()
    {
    return null;
    }

  @Override
  public void setGenomeDirectory(File genomeDirectory)
    {

    }

  @Override
  public File getMutationDirectory()
    {
    return null;
    }

  @Override
  public void setMutationDirectory(File smallMut)
    {

    }

  @Override
  public void addChromosomes(Chromosome[] chromosomes)
    {

    }

  @Override
  public void addChromosome(Chromosome chromosome)
    {

    }

  @Override
  public void replaceChromosome(Chromosome chromosome)
    {

    }

  @Override
  public boolean hasChromosome(String name)
    {
    return false;
    }

  @Override
  public Chromosome[] getChromosomes()
    {
    return new Chromosome[0];
    }

  @Override
  public Chromosome getChromosome(String name)
    {
    return null;
    }

  @Override
  public Mutable mutate(Chromosome chr, int window, FASTAWriter writer)
    {
    return null;
    }




  }




