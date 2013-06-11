package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Location;

/**
 * org.lcsb.lu.igcsa.database.karyotype
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Breakpoint
  {
  static Logger log = Logger.getLogger(Breakpoint.class.getName());

  private String chromosome;
  private String band;
  private Location location;
  private double probability;

  public Breakpoint()
    {}

  public Breakpoint(String chromosome, String band, Location location, double probability)
    {
    this.chromosome = chromosome;
    this.band = band;
    this.location = location;
    this.probability = probability;
    }

  public String getChromosome()
    {
    return chromosome;
    }

  public void setChromosome(String chromosome)
    {
    this.chromosome = chromosome;
    }

  public String getBand()
    {
    return band;
    }

  public void setBand(String band)
    {
    this.band = band;
    }

  public Location getLocation()
    {
    return location;
    }

  public void setLocation(Location location)
    {
    this.location = location;
    }

  public double getProbability()
    {
    return probability;
    }

  public void setProbability(double probability)
    {
    this.probability = probability;
    }
  }
