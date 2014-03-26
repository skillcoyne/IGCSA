/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;

import java.io.DataOutputStream;
import java.io.IOException;

public class FASTAOutputFormat extends FileOutputFormat<LongWritable, Text>
  {
  private static final Log log = LogFactory.getLog(FASTAOutputFormat.class);

  public static final String FASTA_HEADER = "fasta.header";
  public static final String FASTA_LINE_LENGTH = "fasta.line.length";
  public static final String WRITE_HEADER = "write.fasta.header";

  private static final int lineLength = 70;

  private boolean writeHeader = true;

  public static void setLineLength(Job job, int length)
    {
    job.getConfiguration().setInt(FASTA_LINE_LENGTH, length);
    }

  public static void addHeader(Job job, FASTAHeader h)
    {
    job.getConfiguration().set(FASTA_HEADER, h.getFormattedHeader());
    log.info("Adding header " +  h);
    }

  @Override
  public RecordWriter<LongWritable, Text> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException
    {
    if (context.getConfiguration().getInt(WRITE_HEADER, -1) > 0)
      writeHeader = false;

    Path file = new Path( getOutputPath(context), getOutputName(context) );

    FileSystem fs = file.getFileSystem(context.getConfiguration());
    FSDataOutputStream os = fs.create(file);

    log.info(file);

    if (writeHeader)
      {
      String header = context.getConfiguration().get(FASTA_HEADER) + FASTARecordWriter.CARRIAGE_RETURN;
      //String header = context.getConfiguration().get( file.toString().replaceAll("/", ".") ) + FASTARecordWriter.CARRIAGE_RETURN;
      //String header =  + FASTARecordWriter.CARRIAGE_RETURN;
      //String header = context.getConfiguration().get(FASTA_HEADER) + FASTARecordWriter.CARRIAGE_RETURN;
      log.debug(header);
      os.write( header.getBytes() );
      os.flush();
      }

    return new FASTARecordWriter(os, context.getConfiguration().getInt(FASTA_LINE_LENGTH, lineLength), file);
    }

  protected static class FASTARecordWriter extends RecordWriter<LongWritable, Text>
    {
    private static final Log log = LogFactory.getLog(FASTARecordWriter.class);

    // ASCII carriage return (CR), as char
    private static final char CARRIAGE_RETURN = 0x000A;
    // ASCII line feed (LF), as char
    public static final char LINE_FEED = 0x000D;

    private DataOutputStream outStream;
    private int lineLength = 70;

    private int numLines = 0;
    private int counter = 0;
    private int chars = 0;
    private Path path;

    public FASTARecordWriter(DataOutputStream outStream, int lineLength, Path path)
      {
      this.outStream = outStream;
      this.lineLength = lineLength;
      this.path = path;
      }

    private void writeLine(String str, boolean withCR) throws IOException
      {
      if (withCR)
        {
        str = str + CARRIAGE_RETURN;
        ++chars;
        }
      outStream.writeBytes(str);
      ++numLines;

      }

//    @Override
//    public synchronized void write(LongWritable key, Text value) throws IOException
//      {
//      writeLine( key.toString() + "\t" + value.toString(), true);
//      }

    @Override
    public synchronized void write(LongWritable key, Text value) throws IOException
      {
      if (key == null || value == null)
        return;

      StringBuffer buffer = new StringBuffer();
      for (char c: value.toString().toCharArray())
        {
        ++chars;
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
      log.info("***CLOSE*** " + path.toString() + "  Final chars: " + chars + " Lines output: " + numLines);
      outStream.flush();
      outStream.close();
      }



    }


  }
