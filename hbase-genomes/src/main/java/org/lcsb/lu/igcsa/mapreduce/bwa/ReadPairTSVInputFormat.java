package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

/**
 * The reads need to stay in order.  The file can be cut up, but mixing the order of the reads results in fastq files that don't align!
 */
public class ReadPairTSVInputFormat extends FileInputFormat<LongWritable, Text>
  {
  static Logger log = Logger.getLogger(ReadPairTSVInputFormat.class.getName());

  @Override
  public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException
    {
    return new ReadPairRecordReader();
    }

  static class ReadPairRecordReader extends LineRecordReader
    {
    private LongWritable key;
    private Text value;

    @Override
    public synchronized boolean nextKeyValue() throws IOException
      {
      int count = 0;
      value = new Text();

      int max = 10000;

      while (super.nextKeyValue() && count < max)
        {
        Text currVal = super.getCurrentValue();
        value.append(currVal.getBytes(), 0, currVal.getLength());
        value.append("\n".getBytes(), 0, 1);
        key = new LongWritable(super.getCurrentKey().get());
        ++count;
        }

      if (count <= 0)
        {
        key = null;
        value = null;
        return false;
        }
      return true;
      }

    @Override
    public LongWritable getCurrentKey()
      {
      return key;
      }

    @Override
    public Text getCurrentValue()
      {
      return value;
      }

    }
  }
