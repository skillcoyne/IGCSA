package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Band implements Comparable<Band>
  {
  static Logger log = Logger.getLogger(Band.class.getName());

  private String chromosomeName;
  private String bandName;

  private Location location;

  //  public Band()
  //    {}

  public Band(String bandName)
    {
    Pattern p = Pattern.compile("^(\\d+|X|Y)([p|q].*)$");
    Matcher m = p.matcher(bandName);
    if (m.matches())
      {
      this.chromosomeName = m.group(1);
      this.bandName = m.group(2);
      }
    else
      throw new IllegalArgumentException("Band name requires both chromosome and band. Ex: 12q34.1.  Provided: " + bandName);
    }

  public Band(String chr, String band)
    {
    if (chr == null || band == null) throw new IllegalArgumentException("Chromosme and band are required");
    this.chromosomeName = chr;
    this.bandName = band;
    }

  public Band(String chr, String band, Location location)
    {
    if (chr == null || band == null || location == null) throw new IllegalArgumentException("Chromosme, band and location are required");

    this.chromosomeName = chr;
    this.bandName = band;
    this.location = location;
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
    if (b.getChromosomeName().equals(this.getChromosomeName())) return true;
    return false;
    }

  public String whichArm()
    {
    if (bandName.matches("p\\d+")) return "p";
    return "q";
    }

  public int getStart()
    {
    return location.getStart();
    }

  public int getEnd()
    {
    return location.getEnd();
    }


  public String getFullName()
    {
    return chromosomeName + bandName;
    }

  @Override
  public boolean equals(Object o)
    {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Band band = (Band) o;

    boolean equals = true;
    if (bandName != null ? !bandName.equals(band.bandName) : band.bandName != null) equals = false;
    if (chromosomeName != null ? !chromosomeName.equals(band.chromosomeName) : band.chromosomeName != null) equals = false;
    //    if (location != null ? !location.equals(band.location) : band.location != null)
    //      equals = false;

    return equals;
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
    String str = this.chromosomeName + this.bandName;
    if (location != null) str += "<" + this.location.getStart() + "-" + this.location.getEnd() + ">";
    return str;
    }

  @Override
  public int compareTo(Band b)
    {
    if (this.getBandName().equals(b.getBandName())) return 0;
    if (!this.whichArm().equals(b.whichArm())) return this.whichArm().compareTo(b.whichArm());
    else return this.getLocation().compareTo(b.getLocation());
    }

  }
