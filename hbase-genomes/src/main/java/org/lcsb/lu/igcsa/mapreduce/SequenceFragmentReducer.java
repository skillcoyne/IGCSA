/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import org.lcsb.lu.igcsa.genome.Location;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequenceFragmentReducer extends Reducer<SegmentOrderComparator, FragmentWritable, LongWritable, Text>
  {
  private static final Log log = LogFactory.getLog(SequenceFragmentReducer.class);

  private MultipleOutputs mos;

  // NEED THIS - if you fail to close the MultipleOutputs record writers you lose data silently
  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException
    {
    mos.close();
    }

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    mos = new MultipleOutputs(context);
    log.debug(mos);
    }

    @Override
  protected void reduce(SegmentOrderComparator key, Iterable<FragmentWritable> values, Context context) throws IOException, InterruptedException
    {
    log.debug("Order " + key.getOrder() + ":" + key.getSegment());

    // This ensures that the RecordWriter knows which file should have the header written
    context.getConfiguration().set(FASTAOutputFormat.WRITE_HEADER, ""+key.getOrder());

    Iterator<FragmentWritable> fI = values.iterator();
    while (fI.hasNext())
      {
      FragmentWritable fw = fI.next();
      LongWritable segmentKey = new LongWritable(fw.getSegment());

      String namedOutput = Long.toString(key.getOrder());
      mos.write(namedOutput, segmentKey, new Text(fw.getSequence()));
      }
    }

  }
