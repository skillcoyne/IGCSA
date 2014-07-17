/**
 * org.lcsb.lu.igcsa.job
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Arrays;


public class ScoreSAMChain
  {
  private static final Log log = LogFactory.getLog(ScoreSAMChain.class);

  public static void main(String[] args) throws Exception
    {
    log.info(Arrays.toString(args));
    ScoreSAMJob ssj = new ScoreSAMJob();
    ToolRunner.run(new ScoreSAMJob(),new String[]{"-p", args[0], "-o", args[1]}  );
    ToolRunner.run(new ScoreBandRatios(ssj.getConf()), new String[]{args[1] + "/score-files.txt"});
    }



  }
