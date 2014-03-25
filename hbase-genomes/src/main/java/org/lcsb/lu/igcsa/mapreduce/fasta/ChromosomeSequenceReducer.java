package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.GenerateFullGenome;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.mapreduce.SegmentOrderComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
* org.lcsb.lu.igcsa.mapreduce.fasta
* Author: Sarah Killcoyne
* Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
* Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
*/
public class ChromosomeSequenceReducer extends Reducer<SegmentOrderComparator, FragmentWritable, LongWritable, Text>
  {
  private static final Log log = LogFactory.getLog(ChromosomeSequenceReducer.class);

  private MultipleOutputs mos;
  private List<String> chrs;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    mos = new MultipleOutputs(context);
    chrs = new ArrayList<String>(context.getConfiguration().getStringCollection("chromosomes"));
    if (chrs == null || chrs.size() <= 0) throw new IOException("Chromosomes not defined in configuration.");
    log.info("CHROMOSOMES: " + chrs);
    }

  @Override
  protected void reduce(SegmentOrderComparator key, Iterable<FragmentWritable> values, Context context) throws IOException,
  InterruptedException
    {
    //log.debug("ORDER " + key.getOrder() + ":" + key.getSegment());
    Iterator<FragmentWritable> fI = values.iterator();
    while (fI.hasNext())
      {
      FragmentWritable fw = fI.next();
      LongWritable segmentKey = new LongWritable(fw.getSegment());

      String namedOutput = chrs.get((int) key.getOrder());
      mos.write(namedOutput, segmentKey, new Text(fw.getSequence()));
      }
    }

  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException
    {
    mos.close();
    }

  }
