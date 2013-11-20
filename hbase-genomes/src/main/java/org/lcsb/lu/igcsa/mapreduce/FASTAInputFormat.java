/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Logger;

import java.io.IOException;

public class FASTAInputFormat extends FileInputFormat<ImmutableBytesWritable, Text>
  {
  static Logger log = Logger.getLogger(FASTAInputFormat.class.getName());

  @Override
  public RecordReader<ImmutableBytesWritable, Text> createRecordReader(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
    {
    int nucWindow = 1000;
    return new FASTAFragmentRecordReader(nucWindow);
    }


  public static class FASTAFragmentRecordReader extends RecordReader<ImmutableBytesWritable, Text>
    {
    static Logger log = Logger.getLogger(FASTAFragmentRecordReader.class.getName());

    private int window;

    private ImmutableBytesWritable key = new ImmutableBytesWritable();
    private Text value = new Text();

    // The standard gamut of line terminators, plus EOF
    private static final char CARRIAGE_RETURN = 0x000A;
    // ASCII carriage return (CR), as char
    private static final char LINE_FEED = 0x000D;
    // ASCII line feed (LF), as char
    private static final char RECORD_SEPARATOR = 0x001E;
    // ASCII record separator (RS), as char
    private static final char EOF = 0xffff;

    // Reserved characters within the (ASCII) stream
    private final char COMMENT_IDENTIFIER = ';';
    private final char HEADER_IDENTIFIER = '>';


    private CharacterReader reader;
    private FSDataInputStream inputStream;

    private long splitStart;
    private long splitEnd;
    private String splitChr;

    private int positionAdj = 0;

    private long lastStart = 1;

    public FASTAFragmentRecordReader(int window)
      {
      this.window = window;
      }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      splitChr = context.getConfiguration().get("chromosome");

      FileSplit split = (FileSplit) inputSplit;
      Path path = split.getPath();

      splitStart = split.getStart();
      splitEnd = splitStart + split.getLength();

      CompressionCodecFactory factory = new CompressionCodecFactory(context.getConfiguration());
      CompressionCodec codec = factory.getCodec(path);

      inputStream = path.getFileSystem(context.getConfiguration()).open(path);
      inputStream.seek(split.getStart());

      reader = new CharacterReader(inputStream, inputStream.getPos());

      if (codec != null)
        {
        CompressionInputStream is = codec.createInputStream(inputStream);
        reader = new CharacterReader(is);
        }

      // read the header
      if (reader.getPos() == 0)
        {
        if (reader.read() == HEADER_IDENTIFIER)
          {
          String header = reader.readLine();
          log.info(header);
          }
        else  // this actually shouldn't happen but...you're at the beginning of a file that lacks the header?
          {
          reader.reset();
          log.warn("Start of stream (pos 0) in split but no header" + ((FileSplit) inputSplit).getStart());
          }
        }

      log.info("foo");
      }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      if (splitEnd < inputStream.getPos())
        return false;

      long adjustedStart = lastStart;
      ++positionAdj;
      String fragment = reader.readCharacters(window);

      // no more data
      if (fragment == null)
        return false;

      long adjustedEnd = adjustedStart + fragment.length();
      lastStart = adjustedEnd;

      FragmentKey f = new FragmentKey(splitChr, positionAdj, adjustedStart, adjustedEnd);

      key = new ImmutableBytesWritable( FragmentKey.toBytes(f) );
      value = new Text(fragment);

      return true;
      }

    @Override
    public ImmutableBytesWritable getCurrentKey() throws IOException, InterruptedException
      {
      return key;
      }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException
      {
      return value;
      }

    @Override
    public float getProgress() throws IOException, InterruptedException
      {
      return inputStream.getPos(); // for what??
      }

    @Override
    public void close() throws IOException
      {
      //inputStream.close();
      reader.close();
      }

    }

  }
