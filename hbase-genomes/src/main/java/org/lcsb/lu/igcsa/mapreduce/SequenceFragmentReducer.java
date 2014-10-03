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
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;

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
  protected List<Location> locations = new ArrayList<Location>();


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
    log.info(mos);

    Pattern p = Pattern.compile("^.*<(\\d+|X|Y)\\s(\\d+)-(\\d+)>$");
    String[] locs = context.getConfiguration().getStrings(SequenceRequestMapper.CFG_LOC);
    for (String loc : locs)
      {
      Matcher matcher = p.matcher(loc);
      matcher.matches();
      String chr = matcher.group(1);
      int start = Integer.parseInt(matcher.group(2));
      int end = Integer.parseInt(matcher.group(3));

      Location locObj = new Location(chr, start, end);
      locations.add(locObj);
      }
    }

    @Override
  protected void reduce(SegmentOrderComparator key, Iterable<FragmentWritable> values, Context context) throws IOException, InterruptedException
    {
    log.info("Order " + key.getOrder() + ":" + key.getSegment());

    // This ensures that the RecordWriter knows which file should have the header written
    context.getConfiguration().set(FASTAOutputFormat.WRITE_HEADER, ""+key.getOrder());

    for (Iterator<FragmentWritable> fI = values.iterator(); fI.hasNext();)
      {
      FragmentWritable fw = fI.next();
      LongWritable segmentKey = new LongWritable(fw.getSegment());

      String namedOutput = Long.toString(key.getOrder());
      mos.write(namedOutput, segmentKey, new Text(fw.getSequence()));
      }
    }

  }
