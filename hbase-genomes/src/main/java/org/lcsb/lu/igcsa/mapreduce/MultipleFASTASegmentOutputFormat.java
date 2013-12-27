/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.lib.MultipleOutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.util.Progressable;

import java.io.IOException;


public class MultipleFASTASegmentOutputFormat extends MultipleOutputFormat<LongWritable, Text>
  {
  private static final Log log = LogFactory.getLog(MultipleFASTASegmentOutputFormat.class);


  @Override
  protected org.apache.hadoop.mapred.RecordWriter<LongWritable, Text> getBaseRecordWriter(FileSystem fileSystem, JobConf entries, String s, Progressable progressable) throws IOException
    {
    return null;
    }
  }
