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
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;


import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class SequenceRequestMapper extends TableMapper<SegmentOrderComparator, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(SequenceRequestMapper.class);

  public final static String REVERSE = "reverse.segments";
  public final static String CFG_LOC = "location";

  private static List<Location> locations;// = new ArrayList<Location>();

  private static LinkedHashMap<Location, Integer> linkedLocations;

  public static void setLocations(List<Location> locList)
    {
    linkedLocations = new LinkedHashMap<Location, Integer>();

    locations = locList;

    for(Location l:locations)
      linkedLocations.put(l, 0);
    }

  public static void setLocationsToReverse(Location... locs)
    {
    for (Location l: locs)
      linkedLocations.put(l, 1);
    }

//  @Override
//  protected void setup(Context context) throws IOException, InterruptedException
//    {
//    Pattern p = Pattern.compile("^.*<(\\d+|X|Y)\\s(\\d+)-(\\d+)>$");
//    String[] locs = context.getConfiguration().getStrings(CFG_LOC);
//    for (String loc: locs)
//      {
//      Matcher matcher = p.matcher(loc);
//      matcher.matches();
//      String chr = matcher.group(1);
//      int start = Integer.parseInt(matcher.group(2));
//      int end = Integer.parseInt(matcher.group(3));
//
//      locations.add(new Location(chr, start, end));
//      }
//    log.info(locations);
//
//    super.setup(context);
//    }

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
        reverse = (linkedLocations.get(loc) > 0);
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
  }
