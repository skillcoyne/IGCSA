package org.lcsb.lu.igcsa.mapreduce.sam;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
// key is the RFNAME
public class SAMOutputFormat extends FileOutputFormat<Text, SAMCoordinateWritable>
  {
  static Logger log = Logger.getLogger(SAMOutputFormat.class.getName());

  public static final String HEADER_OUTPUT = "sam.header.key.output";

  protected FileOutputCommitter committer;
  /**
   * Flag to track whether anything was output
   */
  protected boolean outputWritten = false;

  @Override
  public synchronized OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException
    { // this is supposed to prevent 0 length "part" files from being written, but doesn't do anything
    if (committer == null)
      {
      Path output = getOutputPath(context);
      committer = new FileOutputCommitter(output, context)
      {
      @Override
      public boolean needsTaskCommit(TaskAttemptContext context) throws IOException
        {
        return outputWritten && super.needsTaskCommit(context);
        }
      };

      }

    return committer;
    }

  @Override
  public RecordWriter getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException
    {
    Path file = new Path(getOutputPath(context), getOutputName(context));

    FileSystem fs = file.getFileSystem(context.getConfiguration());
    final DataOutputStream outStream = fs.create(file);

    return new RecordWriter<Text, Text>()
    {
    @Override
    public void write(Text text, Text record) throws IOException, InterruptedException
      {
      outputWritten = true;
      outStream.writeBytes(record.toString());
      }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException
      {
      outStream.flush();
      outStream.close();
      }
    };
    }

  }
