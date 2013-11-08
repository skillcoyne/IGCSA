/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/*
This reads segments of chromosomes, key should be a location value should be the sequence (text) for that location
 */
public class FASTAInputFormat extends FileInputFormat<SegmentWritable, Text>
  {
  static Logger log = Logger.getLogger(FASTAInputFormat.class.getName());

  @Override
  public RecordReader<SegmentWritable, Text> createRecordReader(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
    {
    return new FASTASegmentReader();
    }


  public static class FASTASegmentReader extends RecordReader<SegmentWritable, Text>
    {
    static Logger log = Logger.getLogger(FASTASegmentReader.class.getName());

    private FSDataInputStream inputStream;
    private LineReader reader;

    private long splitStart;
    private long splitEnd;

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      FileReader ktFile = new FileReader(new File(context.getConfiguration().get("karyotype")));

      FileSplit split = (FileSplit) inputSplit;
      Path path = split.getPath();

      long splitStart = split.getStart();
      long splitEnd = splitStart + split.getLength();

      CompressionCodecFactory factory = new CompressionCodecFactory(context.getConfiguration());
      CompressionCodec codec = factory.getCodec(path);

      inputStream = path.getFileSystem(context.getConfiguration()).open(path);
      inputStream.seek(split.getStart()); // just make sure we're at the start

      reader = new LineReader(inputStream);
      }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      return false;
      }

    @Override
    public SegmentWritable getCurrentKey() throws IOException, InterruptedException
      {
      return null;
      }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException
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
