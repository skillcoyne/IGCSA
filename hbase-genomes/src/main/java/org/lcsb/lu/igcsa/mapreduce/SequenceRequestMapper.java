/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;


import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class SequenceRequestMapper extends TableMapper<SegmentOrderComparator, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(SequenceRequestMapper.class);

  public final static String REVERSE = "reverse.segments";
  public final static String CFG_LOC = "chr.locations";

  private List<Location> locations = new ArrayList<Location>();
  private Map<Location, Boolean> isReversible = new HashMap<Location, Boolean>();

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    Pattern p = Pattern.compile("^.*<(\\d+|X|Y)\\s(\\d+)-(\\d+)>$");
    String[] locs = context.getConfiguration().getStrings(CFG_LOC);
    Set<String> reverseLocs = new HashSet<String>(context.getConfiguration().getStringCollection(REVERSE));

    for (String loc: locs)
      {
      Matcher matcher = p.matcher(loc);
      matcher.matches();
      String chr = matcher.group(1);
      int start = Integer.parseInt(matcher.group(2));
      int end = Integer.parseInt(matcher.group(3));

      Location locObj = new Location(chr, start, end);
      locations.add(locObj);

      isReversible.put(locObj, false);
      if (reverseLocs.contains(loc))
        isReversible.put(locObj, true);
      }
    log.info(locations);
    }

  @Override
  protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
    {
    boolean reverse = false;

    // is there anything to be done with this really?
    String rowId = Bytes.toString(key.get());
    log.debug(rowId + " reverse:" + reverse);

    SequenceResult sr = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);

    int sectionKey = -1;
    for (Location loc: locations)
      {
      if (loc.getChromosome().equals(sr.getChr()) && loc.overlapsLocation( new Location(sr.getStart(), sr.getEnd())) )
        {
        sectionKey = locations.indexOf(loc);
        reverse = isReversible.get(loc);
        }
      }
    if (sectionKey < 0)
      throw new RuntimeException("somehow I didn't match anything and that should never happen!");

    // if reverse the text sequence needs to be reversed and it needs to somehow be indicated with the key I think
    String sequence = sr.getSequence();
    SegmentOrderComparator soc = new SegmentOrderComparator(sectionKey, sr.getSegmentNum());
    if (reverse)
      {
      sequence = new StringBuffer(sequence).reverse().toString();
      soc = new SegmentOrderComparator(sectionKey, (-1*sr.getSegmentNum()));
      }
    FragmentWritable fw = new FragmentWritable(sr.getChr(), sr.getStart(), sr.getEnd(), sr.getSegmentNum(), sequence);

    context.write(soc, fw);
    }


  public static void setLocations(Job job, List<Location> locList)
    {
    List<String> locations = new ArrayList<String>();
    for (Location loc: locList)
      locations.add(loc.toString());

    job.getConfiguration().setStrings(CFG_LOC, locations.toArray(new String[locations.size()]));
    }

  public static void setLocationsToReverse(Job job, Location... locs)
    {
    List<String> locations = new ArrayList<String>();
    for (Location loc: locs)
      locations.add(loc.toString());

    job.getConfiguration().setStrings(REVERSE, locations.toArray(new String[locations.size()]));
    }
  }
