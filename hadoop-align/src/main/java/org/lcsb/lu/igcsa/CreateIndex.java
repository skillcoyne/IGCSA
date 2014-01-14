/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;


public class CreateIndex extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(CreateIndex.class);

  private Configuration conf;

  public static void main(String[] args) throws Exception
    {



    }

  public CreateIndex()
    {
    this.conf = getConf();
    }


  public CreateIndex(Configuration conf)
    {
    this.conf = conf;
    }

  public int run(String[] args) throws Exception
    {
    Job job = new Job(conf, "Generate BWA Index");
//    job.setJarByClass(CreateIndex.class);
//    job.setMapperClass(ChromosomeSequenceMapper.class);
//    job.setReducerClass(ChromosomeSequenceReducer.class);
//    job.setOutputFormatClass(NullOutputFormat.class);





    return 0;
    }
  }
