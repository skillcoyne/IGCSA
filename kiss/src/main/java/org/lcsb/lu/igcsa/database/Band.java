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

  public boolean isCentromere()
    {
    boolean ac = bandName.matches("([p|q])(11|12)") ? true : false;
    return ac;
    }

  public boolean sameChromosome(Band b)
    {
    if (b.getChromosomeName().equals(this.getChromosomeName()))
      return true;
    return false;
    }


  @Override
  public boolean equals(Object o)
    {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Band band = (Band) o;

    if (bandName != null ? !bandName.equals(band.bandName) : band.bandName != null)
      return false;
    if (chromosomeName != null ? !chromosomeName.equals(band.chromosomeName) : band.chromosomeName != null)
      return false;

    return true;
    }

  @Override
  public int hashCode()
    {
    int result = chromosomeName != null ? chromosomeName.hashCode() : 0;
    result = 31 * result + (bandName != null ? bandName.hashCode() : 0);
    return result;
    }

  @Override
  public String toString()
    {
    return this.chromosomeName + this.bandName;
    }
  }
