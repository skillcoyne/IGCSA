/**
 * org.lu.igcsa.hadoop.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;

public class FASTAOutputFormat extends FileOutputFormat<LongWritable, Text>
  {
  static Logger log = Logger.getLogger(FASTAOutputFormat.class.getName());

  protected static class FASTARecordWriter extends RecordWriter<LongWritable, Text>
    {
    static Logger log = Logger.getLogger(FASTARecordWriter.class.getName());

    // The standard gamut of line terminators, plus EOF
    private static final char EOF = 0xffff;

    // ASCII carriage return (CR), as char
    private static final char CARRIAGE_RETURN = 0x000A;
    // ASCII line feed (LF), as char
    private static final char LINE_FEED = 0x000D;


    private DataOutputStream outStream;
    private int lineLength;

    private static int counter = 0;

    public FASTARecordWriter(DataOutputStream outStream, int lineLength)
      {
      this.outStream = outStream;
      this.lineLength = lineLength;
      }

    private void writeLine(String str, boolean withCR) throws IOException
      {
      if (withCR)
        str = str + "\n";

      outStream.write( str.getBytes(), 0, str.getBytes().length );
      //outStream.writeChars(str);
      }

    @Override
    public synchronized void write(LongWritable key, Text value) throws IOException
      {
      if (key == null || value == null)
        return;

      StringBuffer buffer = new StringBuffer();
      for (char c: value.toString().toCharArray())
        {
        buffer.append(c);
        ++counter;
        if (buffer.length() >= lineLength || counter >= lineLength)
          {
          writeLine(buffer.toString(), true);
          buffer = new StringBuffer();
          counter = 0;
          }
        }
      writeLine(buffer.toString(), false);
      }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException
      {
      outStream.writeChar('\n');
      outStream.close();
      }

    }

  @Override
  public RecordWriter<LongWritable, Text> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException
    {
//    context.getOutputKeyClass();
//    context.getOutputValueClass();
    Path file = getDefaultWorkFile(context, "");
    FileSystem fs = file.getFileSystem(context.getConfiguration());

    return new FASTARecordWriter(fs.create(file), 70);
    }




  }
