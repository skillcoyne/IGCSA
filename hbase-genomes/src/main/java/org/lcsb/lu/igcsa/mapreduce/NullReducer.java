package org.lcsb.lu.igcsa.mapreduce;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class NullReducer extends Reducer
  {
  static Logger log = Logger.getLogger(NullReducer.class.getName());

  @Override
  protected void reduce(Object key, Iterable values, Context context) throws IOException, InterruptedException
    {
    super.reduce(key, values, context);
    }
  }
