/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.log4j.Logger;

import java.io.Serializable;

@InterfaceAudience.Public @InterfaceStability.Stable
public class FragmentWritable implements Serializable, Comparable<FragmentWritable> //WritableComparable<FragmentWritable>
  {
  static Logger log = Logger.getLogger(FragmentWritable.class.getName());

  private String chr;
  private long start;
  private long end;
  private long segment;

  private String sequence;

  public FragmentWritable()
    {
    }

  public FragmentWritable(String chr, long start, long end, long seg, String sequence)
    {
    this.chr = chr;
    this.start = start;
    this.end = end;
    this.segment = seg;
    this.sequence = sequence;
    }

  public byte[] write()
    {
    return SerializationUtils.serialize(this);
    }

  public static FragmentWritable read(byte[] bytes)
    {
    return (FragmentWritable) SerializationUtils.deserialize(bytes);
    }


  @Override
  public int compareTo(FragmentWritable fragment)
    {
    if (fragment.equals(this))
      return 0;
    else if (this.getChr().compareTo(fragment.getChr()) != 0)
      return this.getChr().compareTo(fragment.getChr());
    else
      return (this.getStart() > fragment.getStart()) ? 1 : -1;
    }

  @Override
  public boolean equals(Object o)
    {
    FragmentWritable f = (FragmentWritable) o;
    return (f.getChr().equals(this.chr) && f.getStart() == this.start && f.getEnd() == this.end && f.getSegment() == this.segment);
    }


  public String getSequence()
    {
    return sequence;
    }

  public long getSegment()
    {
    return segment;
    }

  public String getChr()
    {
    return chr;
    }

  public long getStart()
    {
    return start;
    }

  public long getEnd()
    {
    return end;
    }

  @Override
  public String toString()
    {
    return chr + ":" + segment + "(" + start + "-" + end + ")";
    }


  }
