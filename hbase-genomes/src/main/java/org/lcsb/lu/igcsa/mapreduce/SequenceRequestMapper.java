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
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequenceRequestMapper extends TableMapper<LongWritable, Text>
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

    SequenceResult sequence = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);

    // check how much of the sequence should be output and in what order


    context.write(new LongWritable(sequence.getSegmentNum()), new Text(sequence.getSequence()) );

    super.map(key, value, context);
    }
  }
