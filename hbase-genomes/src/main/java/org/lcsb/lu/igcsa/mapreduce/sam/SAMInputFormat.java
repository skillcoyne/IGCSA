/**
 * org.lcsb.lu.igcsa.mapreduce.sam
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.sam;

import net.sf.samtools.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.mapreduce.*;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.jruby.ext.posix.FileStat;
import org.lcsb.lu.igcsa.job.ScoreSAMJob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SAMInputFormat extends FileInputFormat<LongWritable, SAMRecordWritable>
  {
  private static final Log log = LogFactory.getLog(SAMInputFormat.class);

  public static String[] addSAMInputs(Job job, Path path) throws IOException
    {
    final FileSystem fs = FileSystem.get(path.toUri(), job.getConfiguration());

    FileStatus[] statuses = fs.globStatus(new Path(path, "*/*"), new PathFilter()
    {
    @Override
    public boolean accept(Path path)
      {
      if (path.getName().toString().endsWith(".sam") || path.getName().toString().endsWith("bam"))
        {
        return true;
        }
      return false;
      }
    });
    if (statuses.length <=0)
      throw new IOException("No bam/sam files in " + path.toString());

    Set<String> sequenceNames = new HashSet<String>();
    Configuration conf = job.getConfiguration();
    List<String> samFiles = new ArrayList<String>();
    for (int i=0; i<statuses.length; i++)
      {
      FileStatus status = statuses[i];

      SAMFileReader reader = new SAMFileReader(fs.open(status.getPath()));
      SAMFileHeader header = reader.getFileHeader();

      SAMSequenceRecord sqInfo = header.getSequenceDictionary().getSequence(0);
      int sequenceLength = sqInfo.getSequenceLength();

      if (!sqInfo.getSequenceName().contains("bp="))
        throw new IOException("No breakpoint information in the sequence name. Band scores cannot be calculated: " + sqInfo.getSequenceName());

      int bpLocation = Integer.parseInt(sqInfo.getSequenceName().split("bp=")[1]);
      String name = sqInfo.getSequenceName();

      if (sequenceNames.contains(name))
        log.error("A bam/sam file already exists with this sequence name, skipping: " + status.getPath());

      samFiles.add(status.getPath().toString());

      conf.setInt(i + "." + ScoreSAMJob.SEQ_LEN, sequenceLength);
      conf.setInt(i + "." + ScoreSAMJob.BP_LOC, bpLocation);

      conf.setInt(i + "." + ScoreSAMJob.LEFT, bpLocation); //left
      conf.setInt(i + "." + ScoreSAMJob.RIGHT, sequenceLength - bpLocation); //right

      conf.set(i + "." + ScoreSAMJob.SEQ_NAME, name);

      reader.close();
      FileInputFormat.addInputPath(job, status.getPath());
      }

    conf.setStrings(ScoreSAMJob.INPUT_NAMES, samFiles.toArray(new String[samFiles.size()]));
    return samFiles.toArray(new String[samFiles.size()]);
    }

  @Override
  public RecordReader<LongWritable, SAMRecordWritable> createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException
    {
    return new SAMRecordReader();
    }

  // Since I can't create a SAMRecord without a SAMFileHeader it's just easier to do this for now
  @Override
  protected boolean isSplitable(JobContext context, Path filename)
    {
    return false;
    }

  public static class SAMRecordReader extends RecordReader<LongWritable, SAMRecordWritable>
    {
    private SAMFileReader samReader;
    private SAMRecordIterator recordIterator;
    private long readNum;

    private LongWritable key = new LongWritable();
    private SAMRecordWritable value = new SAMRecordWritable();
    private FSDataInputStream inputStream;
    private long splitEnd;

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      FileSplit split = (FileSplit) inputSplit;
      Path path = split.getPath();

      log.info("Reading " + path.toString());

      splitEnd = split.getStart() + split.getLength();

      CompressionCodecFactory factory = new CompressionCodecFactory(context.getConfiguration());
      CompressionCodec codec = factory.getCodec(path);

      inputStream = path.getFileSystem(context.getConfiguration()).open(path);
      if (codec != null)
        {
        CompressionInputStream is = codec.createInputStream(inputStream);
        samReader = new SAMFileReader(is);
        }
      else
        samReader = new SAMFileReader(inputStream);

      recordIterator = samReader.iterator();
      readNum = 0L;
      }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      if (recordIterator.hasNext())
        {
        SAMRecord record = recordIterator.next();
        ++readNum;

        key = new LongWritable(readNum);
        value = new SAMRecordWritable(record);

        return true;
        }

      return false;
      }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException
      {
      return key;
      }

    @Override
    public SAMRecordWritable getCurrentValue() throws IOException, InterruptedException
      {
      return value;
      }

    @Override
    public float getProgress() throws IOException, InterruptedException
      {
      float progress = 0.0f;
      if (inputStream.getPos() > 0)
        {
        progress = Math.min(1.0f, (float) inputStream.getPos() / splitEnd);
        }
      return progress;
      }

    @Override
    public void close() throws IOException
      {
      samReader.close();
      }
    }

  }
