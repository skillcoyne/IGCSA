package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Location;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Band
  {
  static Logger log = Logger.getLogger(Band.class.getName());

  private String chromosomeName;
  private String bandName;

  private Location location;

  public Band()
    {}

  public Band(String chr, String band)
    {
    this.chromosomeName = chr;
    this.bandName = band;
    }

  public String getChromosomeName()
    {
    return chromosomeName;
    }

  public void setChromosomeName(String chromosomeName)
    {
    this.chromosomeName = chromosomeName;
    }

  public String getBandName()
    {
    return bandName;
    }

  public void setBandName(String bandName)
    {
    this.bandName = bandName;
    }

  public Location getLocation()
    {
    return location;
    }

  public void setLocation(Location location)
    {
    this.location = location;
    }

  @Override
  public String toString()
    {
    return "<" + this.chromosomeName + this.bandName + ">" + " " + this.getClass().getName() + this.hashCode();
    }
  }
