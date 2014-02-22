package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;

import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce.fasta
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTALinesInputFormat extends FileInputFormat<LongWritable, FragmentWritable>
  {
  static Logger log = Logger.getLogger(FASTALinesInputFormat.class.getName());

  public void FASTALinesInputFormat()
    {

    }

  @Override
  public RecordReader<LongWritable, FragmentWritable> createRecordReader(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
    {
    return null;
    }

  static class FASTARecordReader extends RecordReader<LongWritable, FragmentWritable>
    {
    LineRecordReader rr;

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      return false;
      }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException
      {
      return null;
      }

    @Override
    public FragmentWritable getCurrentValue() throws IOException, InterruptedException
      {
      return null;
      }

    @Override
    public float getProgress() throws IOException, InterruptedException
      {
      return 0;
      }

    @Override
    public void close() throws IOException
      {

      }
    }

  }
