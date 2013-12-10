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
  private static final Log log = LogFactory.getLog(FASTAOutputFormat.class);

  public static final String FASTA_HEADER = "fasta.header";
  public static final String FASTA_LINE_LENGTH = "fasta.line.length";

  private static int lineLength = 70;

  @Override
  public RecordWriter<LongWritable, Text> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException
    {
    if (context.getConfiguration().get(FASTA_LINE_LENGTH) != null)
      lineLength = Integer.parseInt(context.getConfiguration().get(FASTA_LINE_LENGTH));

    Path file = getDefaultWorkFile(context, "");
    FileSystem fs = file.getFileSystem(context.getConfiguration());

    DataOutputStream os = fs.create(file);
    String header = context.getConfiguration().get(FASTA_HEADER) + FASTARecordWriter.LINE_FEED;
    os.write( header.getBytes() );
    os.flush();

    return new FASTARecordWriter(os, lineLength);
    }


  protected static class FASTARecordWriter extends RecordWriter<LongWritable, Text>
    {
    private static final Log log = LogFactory.getLog(FASTARecordWriter.class);

    // ASCII carriage return (CR), as char
    private static final char CARRIAGE_RETURN = 0x000A;
    // ASCII line feed (LF), as char
    public static final char LINE_FEED = 0x000D;

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
        str = str + LINE_FEED;

      outStream.write( str.getBytes(), 0, str.getBytes().length );
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


  }
