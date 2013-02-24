package org.lcsb.lu.igcsa.genome;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Location
  {
  private int start;
  private int end;

  public Location(int s, int e) throws IllegalArgumentException
    {
    this.start = s; this.end = e;

    if (start > end)
      {
      throw new IllegalArgumentException("The start position should come before the end position.");
      }
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
  }
