package org.lcsb.lu.igcsa.mapreduce;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.log4j.Logger;


/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FragmentPartitioner extends Partitioner<SegmentOrderComparator, LongWritable>
  {
  static Logger log = Logger.getLogger(FragmentPartitioner.class.getName());

  @Override
  public int getPartition(SegmentOrderComparator soc, LongWritable value, int numPartitions)
    {
    long order = soc.getOrder();
    if (order > numPartitions)
      throw new RuntimeException("Not enough partitions (" + numPartitions + ") for " + order );
    return (int)order;
    }
  }
