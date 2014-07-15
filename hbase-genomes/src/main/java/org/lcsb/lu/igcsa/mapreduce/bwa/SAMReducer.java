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
    return (key.toString().equals("*"))? "zzz": key.toString().replaceAll("\\W", "");
    }

  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
    {
    log.info("Reducing " + key);
    Iterator<Text> sI = values.iterator();
    while (sI.hasNext())
      {
      Text scw = sI.next();
      if (scw.getLength() <= 0)
        {
        // I'm sure there's a smarter way to do this, but the header was being output once for ever section of the read file that had been processed.
        // But the header is always identical so this was an issue
        if (!context.getConfiguration().getBoolean(SAMOutputFormat.HEADER_OUTPUT, false))
          {
          context.getConfiguration().setBoolean(SAMOutputFormat.HEADER_OUTPUT, true);
          mos.write(key, key, "0");
          }
        }
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
