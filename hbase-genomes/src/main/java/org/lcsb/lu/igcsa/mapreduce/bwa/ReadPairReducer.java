package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.BWAAlign;
import org.lcsb.lu.igcsa.ThreadedStreamConnector;

import java.io.*;
import java.util.Iterator;


/**
* org.lcsb.lu.igcsa.mapreduce.bwa
* Author: Sarah Killcoyne
* Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
* Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
*/
public class ReadPairReducer extends Reducer<Text, Text, Text, Text>
  {
  static Logger log = Logger.getLogger(ReadPairReducer.class.getName());

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    super.setup(context);
    }

  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
    {
    log.info("SAM HEADER: " + key);
    int count = 0;
    Text out = new Text();
    Iterator<Text> tI = values.iterator();
    while (tI.hasNext())
      {
      Text t = tI.next();
      out.append(t.getBytes(), 0, t.getLength());
      ++count;
      }
    log.info("SAM lines: " + count );
    context.write(key, out);
    }


  }
