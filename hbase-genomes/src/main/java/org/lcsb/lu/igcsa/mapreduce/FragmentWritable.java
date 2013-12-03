/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.io.WritableComparable;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

@InterfaceAudience.Public @InterfaceStability.Stable
public class FragmentWritable implements WritableComparable<FragmentWritable> //Serializable, Comparable<FragmentWritable> //WritableComparable<FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(FragmentWritable.class);

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

  @Override
  public void write(DataOutput output) throws IOException
    {
    output.writeChars(chr);
    output.writeLong(start);
    output.writeLong(end);
    output.writeLong(segment);
    output.writeChars(sequence);
    }

  @Override
  public void readFields(DataInput dataInput) throws IOException
    {
    chr = dataInput.readLine();
    start = dataInput.readLong();
    end = dataInput.readLong();
    segment = dataInput.readLong();
    sequence = dataInput.readLine();
    }

  public static FragmentWritable read(DataInput dataInput) throws IOException
    {
    String chr = dataInput.readLine();
    long start = dataInput.readLong();
    long end = dataInput.readLong();
    long segment = dataInput.readLong();
    String sequence = dataInput.readLine();
    return new FragmentWritable(chr, start, end, segment,  sequence);
    }


  //  public byte[] write()
//    {
//    return SerializationUtils.serialize(this);
//    }

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
