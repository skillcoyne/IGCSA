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
public class FragmentPartitioner extends Partitioner<SegmentOrderComparator, FragmentWritable>
  {
  static Logger log = Logger.getLogger(FragmentPartitioner.class.getName());

  @Override
  public int getPartition(SegmentOrderComparator soc, FragmentWritable value, int numPartitions)
    {
    int order = (int) soc.getOrder();

    if (numPartitions == 1) // only happens in my test (via the IDE) I think...could happen in a one node test system too though
      return 0;
    // Since my partitioners are 0..n, when n == numpartions, mod n == 0 so have to avoid that
    else
      return (order == numPartitions)? order: order % numPartitions;
    }
  }
