package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.avro.ipc.trace.FileSpanStorage;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.BWAAlign;

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
public class SubSeqKeyValueInputFormat extends FileInputFormat<LongWritable, Text>
  {
  static Logger log = Logger.getLogger(SubSeqKeyValueInputFormat.class.getName());


  @Override
  public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException
    {
    return new ReadPairRecordReader(context.getConfiguration());
    //return new NLinesRecordReader();
    }

  static class ReadPairRecordReader extends RecordReader<LongWritable, Text>
    {
    private LineRecordReader recordReader;
    private LongWritable key;
    private Text value;

    private FileSplit split;

    public ReadPairRecordReader(Configuration conf) throws IOException
      {
      recordReader = new LineRecordReader();
      }

    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException
      {
      recordReader.initialize(genericSplit, context);
      split = (FileSplit) genericSplit;
      }


    @Override
    public float getProgress() throws IOException, InterruptedException
      {
      return recordReader.getProgress();
      }

    @Override
    public void close() throws IOException
      {
      recordReader.close();
      }

    @Override
    public synchronized boolean nextKeyValue() throws IOException
      {
      int count = 0;
      value = new Text();
      while (recordReader.nextKeyValue() && count < 3000)
        {
        Text currVal = recordReader.getCurrentValue();
        value.append(currVal.getBytes(), 0, currVal.getLength());
        value.append("\n".getBytes(), 0, 1);
        key = recordReader.getCurrentKey();
        ++count;
        }

//      if (value.getLength() <= 1)
//        {
//        key = null;       // this just causes the job to hang and I don't know why
//        value = null;
//        return false;
//        }
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
