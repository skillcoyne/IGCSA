/**
 * org.lcsb.lu.igcsa.mapreduce.fasta
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.mapreduce.SegmentOrderComparator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class SingleChromosomeReducer extends Reducer<SegmentOrderComparator, FragmentWritable, LongWritable, Text>
  {
  private static final Log log = LogFactory.getLog(SingleChromosomeReducer.class);

  @Override
  protected void reduce(SegmentOrderComparator key, Iterable<FragmentWritable> values, Context context) throws IOException, InterruptedException
    {
    //log.info("ORDER " + key.getOrder() + ":" + key.getSegment());
    Iterator<FragmentWritable> fI = values.iterator();
    while (fI.hasNext())
      {
      FragmentWritable fw = fI.next();
      LongWritable segmentKey = new LongWritable(fw.getSegment());

      context.write(segmentKey, new Text(fw.getSequence()));
      }
    }

  }
