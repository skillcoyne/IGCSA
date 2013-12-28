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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class SequenceRequestMapper extends TableMapper<SequenceFragmentReducer.SegmentOrderComparator, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(SequenceRequestMapper.class);

  public final static String CFG_ABR = "aberration.type";
  public final static String CFG_LOC = "location";

  private List<Location> locations = new ArrayList<Location>();

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
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
  protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
    {
    boolean reverse = (context.getConfiguration().get(CFG_ABR).equals("inv")) ;

    // is there anything to be done with this really?
    String rowId = Bytes.toString(key.get());
    log.info(rowId + " reverse:" + reverse);

    SequenceResult sr = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);

    int sectionKey = -1;
    for (Location loc: locations)
      {
      if (loc.getChromosome().equals(sr.getChr()) && loc.overlapsLocation( new Location(sr.getStart(), sr.getEnd())) )
        sectionKey = locations.indexOf(loc);
      }
    if (sectionKey < 0)
      throw new RuntimeException("somehow I didn't match anything and that should never happen!");

    // if reverse the text sequence needs to be reversed and it needs to somehow be indicated with the key I think
    String sequence = sr.getSequence();
    if (reverse)
      sequence = new StringBuffer(sequence).reverse().toString();

    SequenceFragmentReducer.SegmentOrderComparator soc = new SequenceFragmentReducer.SegmentOrderComparator(sectionKey, sr.getSegmentNum());
    if (reverse)
      soc = new SequenceFragmentReducer.SegmentOrderComparator(sectionKey, (-1*sr.getSegmentNum()));



    FragmentWritable fw = new FragmentWritable(sr.getChr(), sr.getStart(), sr.getEnd(), sr.getSegmentNum(), sequence);

    // TODO check how much of the sequence should be output and in what order
    context.write(soc, fw);
    }
  }
