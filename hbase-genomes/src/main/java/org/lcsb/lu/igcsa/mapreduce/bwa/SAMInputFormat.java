package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.mapreduce.fasta.CharacterReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SAMInputFormat extends FileInputFormat<LongWritable, Text>
  {
  static Logger log = Logger.getLogger(SAMInputFormat.class.getName());

  public static final String CONF_HEADER = "sam.record.header";

  @Override
  public RecordReader<LongWritable, Text> createRecordReader(InputSplit inputSplit, TaskAttemptContext context) throws IOException,
  InterruptedException
    {
    return new SAMRecordReader();
    }

  static class SAMRecordReader extends RecordReader<LongWritable, Text>
    {
    private LongWritable key;
    private Text value;

    private FSDataInputStream inputStream;
    //private LineReader reader;
    private BufferedReader reader;

    private long start, end, position;
    private Configuration conf;

    private void openReader(Path path, long seek) throws IOException
      {
      if (inputStream != null)
        {
        inputStream.close();
        reader.close();
        }

      inputStream = path.getFileSystem(conf).open(path);
      inputStream.seek(seek);
      reader = new BufferedReader(new InputStreamReader(inputStream));
      }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      FileSplit split = (FileSplit) inputSplit;

      start = split.getStart();
      end = start + split.getLength();

      conf = context.getConfiguration();
      openReader(split.getPath(), start);

      if (start != 0)
        {
        // If this is not the first split, we always throw away first record
        // because we always (except the last split) read one extra line in
        // next() method.
        int skip = (int) Math.min(Integer.MAX_VALUE, end - start); // just want to see what this is
        start += reader.readLine().getBytes().length;
        }
      else
        {
        // get the header
        List<String> header = new ArrayList<String>();
        String line;
        int headerLength = 0;
        while ((line = reader.readLine()).startsWith("@"))
          {
          headerLength += line.length();
          header.add(line);
          }
        conf.setStrings(CONF_HEADER, header.toArray(new String[header.size()]));
        // reset the reader
        openReader(split.getPath(), (long) headerLength);
        }
      }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      key = new LongWritable(inputStream.getPos());
      value = new Text();

      if (inputStream.getPos() <= end)
        {
        // reads one line past the end of the split
        String line = reader.readLine() + "\n";
        value.append(line.getBytes(), 0, line.length());
        }

      if (value.getLength() <= 0)
        {
        key = null;
        value = null;
        return false;
        }

      return true;
      }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException
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
      return inputStream.getPos() / end;
      }

    @Override
    public void close() throws IOException
      {
      inputStream.close();
      reader.close();
      }
    }

  }
