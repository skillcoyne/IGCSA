package org.lcsb.lu.igcsa.mapreduce.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.mapreduce.job
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public abstract class Job extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(Job.class.getName());


  public abstract int runJob(Configuration conf, String[] args) throws Exception;

  @Override
  public int run(String[] args) throws Exception
    {
    final long startTime = System.currentTimeMillis();
    int rj = runJob(getConf(), args);
    final long elapsedTime = System.currentTimeMillis() - startTime;
    log.info("Finished job " + elapsedTime / 1000 + " seconds");
    return rj;
    }

  }
