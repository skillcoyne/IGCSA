package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ChromosomeFragment
  {
  static Logger log = Logger.getLogger(ChromosomeFragment.class.getName());

  private String chromosome;
  private String band;
  private Location bandLocation;

  public ChromosomeFragment(String chromosome, String band, Location bandLocation)
    {
    this.chromosome = chromosome;
    this.band = band;
    this.bandLocation = bandLocation;
    }

  public String getChromosome()
    {
    return chromosome;
    }

  public String getBand()
    {
    return band;
    }

  public Location getBandLocation()
    {
    return bandLocation;
    }
  }
