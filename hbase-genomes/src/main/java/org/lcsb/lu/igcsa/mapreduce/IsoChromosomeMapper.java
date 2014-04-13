package org.lcsb.lu.igcsa.mapreduce;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.genomes.SequenceResult;

import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class IsoChromosomeMapper extends SequenceRequestMapper
  {
  static Logger log = Logger.getLogger(IsoChromosomeMapper.class.getName());

  public static final String REV_FIRST = "reverse.first";


  private void writeSegment(SequenceResult sr, SegmentOrderComparator soc, Context context) throws IOException, InterruptedException
    {
    String sequence = sr.getSequence();
    FragmentWritable fw = new FragmentWritable(sr.getChr(), sr.getStart(), sr.getEnd(), sr.getSegmentNum(), sequence);
    context.write(soc, fw);
    }

  @Override
  protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
    {
    // is there anything to be done with this really?
    String rowId = Bytes.toString(key.get());
    // iso chr is a special case.  If you start at p then section = 0 then the reverse = 1.
    // if you start at q then reverse = 0 and forward = 1.
    // I can write the sequence twice and maybe the answer is it's just a special case
    SequenceResult sr = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);
    if (context.getConfiguration().getBoolean(REV_FIRST, false))
      {
      writeSegment(sr, new SegmentOrderComparator(0, (-1 * sr.getSegmentNum())), context);
      writeSegment(sr, new SegmentOrderComparator(1, sr.getSegmentNum()), context);
      }
    else
      {
      writeSegment(sr, new SegmentOrderComparator(0, sr.getSegmentNum()), context);
      writeSegment(sr, new SegmentOrderComparator(1, (-1 * sr.getSegmentNum())), context);
      }
    }

  public static void reverseFirst(boolean r, Job job)
    {
    job.getConfiguration().setBoolean(REV_FIRST, r);
    }

  }
