package org.lcsb.lu.igcsa.genome;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.Range;
import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.tables
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Location implements Comparable<Location>
  {
  static Logger log = Logger.getLogger(Location.class.getName());

  private String chromosome = "";

  private int start;
  private int end;

  protected Range range;

  public Location(String chr, int s, int e) throws IllegalArgumentException
    {
    this(s,e);
    chromosome = chr;
    }

  public Location(int s, int e) throws IllegalArgumentException
    {
    this.start = s; this.end = e;

    range = new IntRange(s, e);

    if (start > end) throw new IllegalArgumentException("The start position should come before the end position.");
    }

  public String getChromosome()
    {
    return chromosome;
    }

  public int getStart()
    {
    return start;
    }

  public int getEnd()
    {
    return end;
    }

  public int getLength()
    {
    return end - start;
    }

  public boolean containsLocation(Location location)
    {
    return this.range.containsRange(location.range);
    }

  public boolean overlapsLocation(Location location)
    {
    return this.range.overlapsRange(location.range);
    }

  @Override
  public boolean equals(Object obj)
    {
    Location loc = (Location) obj;
    return ( (loc.getStart() == this.getStart()) && (loc.getEnd() == this.getEnd()) );
    }

  @Override
  public int hashCode()
    {
    int result = start;
    result = 31 * result + end;
    return result;
    }

  @Override
  public String toString()
    {
    return super.toString() + ": <" + this.getChromosome() + " " + this.getStart() + "-" + this.getEnd() + ">";
    }

  @Override
  public int compareTo(Location location)
    {
    int compare = 0;
    //if (this.equals(location)) return 0;
    if ( (this.start == location.getStart() && this.end > location.getEnd() ) ||
              (this.start > location.getStart()) ) compare = 1;
    else if ( (this.start < location.getStart() ) ) compare = -1;
    return compare;
    }
  }
