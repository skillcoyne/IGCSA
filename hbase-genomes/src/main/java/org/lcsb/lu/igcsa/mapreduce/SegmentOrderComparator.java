/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/*
Order is not determined by the sort order of the chromosomes, but the order of the location array so...
*/

public class SegmentOrderComparator implements WritableComparable<SegmentOrderComparator>
  {
  private static final Log log = LogFactory.getLog(SegmentOrderComparator.class);
  protected int order;
  protected long segment;

  public SegmentOrderComparator()
    {}

  public SegmentOrderComparator(int order, long segment)
    {
    this.segment = segment;
    this.order = order;
    }


  public long getSegment()
    {
    return segment;
    }

  public long getOrder()
    {
    return order;
    }

  @Override
  public int compareTo(SegmentOrderComparator scc)
    {
    if (scc.equals(this))
      return 0;
    else if (this.order != scc.getOrder())
      return (this.order > scc.getOrder()) ? 1: -1;
    else
      return (this.segment > scc.getSegment()) ? 1: -1;
    }

  @Override
  public boolean equals(Object o)
    {
    SegmentOrderComparator scc = (SegmentOrderComparator) o;
    return (this.order == scc.getOrder() && this.segment == scc.getSegment());
    }

  @Override
  public void write(DataOutput output) throws IOException
    {
    output.writeInt(order);
    output.writeLong(segment);
    }

  @Override
  public void readFields(DataInput dataInput) throws IOException
    {
    order = dataInput.readInt();
    segment = dataInput.readLong();
    }
  }
