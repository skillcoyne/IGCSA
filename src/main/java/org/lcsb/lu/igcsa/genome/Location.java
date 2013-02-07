package org.lcsb.lu.igcsa.genome;

/**
 * org.lcsb.lu.igcsa.genome
 * User: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open source License Apache 2
 */
public class Location
  {
  private int Start;
  private int End;
  private int Length;

  public Location(int s, int e, Chromosome chr) throws Exception
    {
    this.Start = s; this.End = e;

    if (Start > End)
      {
      throw new Exception("The Start position should come before the End position.");
      }

    this.Length = End - Start;
    }

  public int getStart()
    {
    return Start;
    }

  public int getEnd()
    {
    return End;
    }

  public int getLength()
    {
    return Length;
    }

  }
