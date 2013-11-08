/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.hadoop.io.BinaryComparable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SegmentWritable  extends BinaryComparable implements WritableComparable<BinaryComparable>
  {
  static Logger log = Logger.getLogger(SegmentWritable.class.getName());

  private int writeOrder;

  private String chr;
  private long start;
  private long end;

  public SegmentWritable(int writeOrder)
    {
    this.writeOrder = writeOrder;
    }

  @Override
  public void write(DataOutput output) throws IOException
    {
    output.writeChars(chr);
    output.writeLong(start);
    output.writeLong(end);
    }

  @Override
  public void readFields(DataInput input) throws IOException
    {
    chr = input.readLine();
    start = input.readLong();
    end = input.readLong();
    }

  @Override
  public int getLength()
    {
    return (int)(end-start);
    }

  @Override
  public byte[] getBytes()
    {
    return new byte[0];
    }

  @Override
  public int compareTo(BinaryComparable other)
    {
    SegmentWritable o = (SegmentWritable) other;
    return this.writeOrder > o.writeOrder ? 1 : this.writeOrder < o.writeOrder ? -1 : 0;
    }
  }
