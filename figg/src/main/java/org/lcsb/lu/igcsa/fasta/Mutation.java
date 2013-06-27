package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.database.insilico
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Mutation
  {
  static Logger log = Logger.getLogger(Mutation.class.getName());

  private int genomeId;
  private String chromosome;
  private int fragment;
  private int startLocation;
  private int endLocation;
  private String variationType;
  private String sequence;
  private int gcBin;
  private int count;


  public Mutation()
    {}

  public Mutation(String chromosome, int count)
    {
    this.chromosome = chromosome;
    this.count = count;
    }

  public Mutation(String chromosome, int startLocation, int endLocation, String variationType)
    {
    this.chromosome = chromosome;
    this.startLocation = startLocation;
    this.endLocation = endLocation;
    this.variationType = variationType;
    }

  public int getCount()
    {
    return count;
    }

  public void setCount(int count)
    {
    this.count = count;
    }

  public int getGenomeId()
    {
    return genomeId;
    }

  public void setGenomeId(int genomeId)
    {
    this.genomeId = genomeId;
    }

  public String getChromosome()
    {
    return chromosome;
    }

  public void setChromosome(String chromosome)
    {
    this.chromosome = chromosome;
    }

  public int getFragment()
    {
    return fragment;
    }

  public void setFragment(int fragment)
    {
    this.fragment = fragment;
    }

  public int getStartLocation()
    {
    return startLocation;
    }

  public void setStartLocation(int startLocation)
    {
    this.startLocation = startLocation;
    }

  public int getEndLocation()
    {
    return endLocation;
    }

  public void setEndLocation(int endLocation)
    {
    this.endLocation = endLocation;
    }

  public String getVariationType()
    {
    return variationType;
    }

  public void setVariationType(String variationType)
    {
    this.variationType = variationType;
    }

  public String getSequence()
    {
    return sequence;
    }

  public void setSequence(String sequence)
    {
    this.sequence = sequence;
    }

  public int getGCBin()
    {
    return gcBin;
    }

  public void setGCBin(int gcBin)
    {
    this.gcBin = gcBin;
    }
  }
