package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SAMReducer extends Reducer<Text, Text, Text, Text>
  {
  static Logger log = Logger.getLogger(SAMReducer.class.getName());

  private MultipleOutputs mos;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    mos = new MultipleOutputs(context);
    }

  protected String generateFileName(Text key)
    {
    return (key.toString().equals("*"))? "ZZZ": key.toString().replace("|", "") ;
    }

  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
    {
    Iterator<Text> sI = values.iterator();
    while (sI.hasNext())
      {
      Text scw = sI.next();
      if (scw.getLength() <= 0)
        mos.write(key, key, "AAA");
      else
        mos.write(key, scw, generateFileName(key));
      }
    }

  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException
    {
    mos.close();
    }
  }
