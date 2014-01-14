/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;


public class NullMapper extends Mapper
  {
  private static final Log log = LogFactory.getLog(NullMapper.class);

  protected void map(Object key, Object value, Context context) throws IOException, InterruptedException
    {
    super.map(key, value, context);
    }
  }
