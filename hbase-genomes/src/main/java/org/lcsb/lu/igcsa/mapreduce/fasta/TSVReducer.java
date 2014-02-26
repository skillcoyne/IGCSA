package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;

import java.io.IOException;
import java.util.Iterator;


/**
 * org.lcsb.lu.igcsa.mapreduce.fasta
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TSVReducer extends Reducer<Text, LongWritable, Text, Text>
  {
  private MultipleOutputs mos;
  private String genomeName;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    mos = new MultipleOutputs(context);
    genomeName = context.getConfiguration().get("genome");
    }

  @Override
  protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException
    {
    Iterator<LongWritable> lI = values.iterator();
    long length = 0;
    int segments = 0;
    while (lI.hasNext())
      {
      ++segments;
      length += lI.next().get();
      }

    String rowId = ChromosomeRow.createRowId(genomeName, key.toString());
    mos.write("chromosome", new Text(rowId), new Text(genomeName + "\t" + key + "\t" + length + "\t" + segments));
    }

  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException
    {
    mos.close();
    }

  }
