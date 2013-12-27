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


public class SequenceFragmentReducer extends Reducer<SequenceFragmentReducer.SegmentOrderComparator, FragmentWritable, LongWritable, Text>
  {
  private static final Log log = LogFactory.getLog(SequenceFragmentReducer.class);

  public final static String CFG_ABR = "aberration.type";
  public final static String CFG_LOC = "location";

  private List<Location> locations = new ArrayList<Location>();

  private MultipleOutputs mos;


  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    mos = new MultipleOutputs(context);

    Pattern p = Pattern.compile("^.*<(\\d+|X|Y)\\s(\\d+)-(\\d+)>$");
    String[] locs = context.getConfiguration().getStrings(CFG_LOC);
    for (String loc: locs)
      {
      Matcher matcher = p.matcher(loc);
      matcher.matches();
      String chr = matcher.group(1);
      int start = Integer.parseInt(matcher.group(2));
      int end = Integer.parseInt(matcher.group(3));

      locations.add(new Location(chr, start, end));
      }
    super.setup(context);
    }

  @Override
  protected void reduce(SegmentOrderComparator key, Iterable<FragmentWritable> values, Context context) throws IOException, InterruptedException
    {
    boolean reverse = (context.getConfiguration().get(CFG_ABR).equals("inv")) ;

    log.info("Order " + key.getOrder() + ":" + key.getSegment());

    Iterator<FragmentWritable> fI = values.iterator();
    while (fI.hasNext())
      {
      FragmentWritable fw = fI.next();
      LongWritable segmentKey = new LongWritable(fw.getSegment());
      //log.info(fw.getChr() + " : " + fw.getSegment() + " " +  fw.getStart() + "-" + fw.getEnd());
      context.write(segmentKey, new Text(fw.getSequence()) );
      }

    }

  /*
  Order is not determined by the sort order of the chromosomes, but the order of the location array so...
   */
  public static class SegmentOrderComparator implements WritableComparable<SegmentOrderComparator>
    {
    private int order;
    private long segment;


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


  }
