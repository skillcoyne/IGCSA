/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.population.utils.FileUtils;


import java.io.IOException;

public class FASTAFragmentInputFormat extends FileInputFormat<LongWritable, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(FASTAFragmentInputFormat.class);

  @Override
  protected boolean isSplitable(JobContext context, Path filename)
    {
    // force the entire file to be read in a single split. This is mostly just convinience.  Otherwise the last record in the split is going to consume some bytes from the next split, and figuring out how many bytes were consumed and seeking to the right spot is just not something I feel like doing right now...
    return false;
    }

  @Override
  public RecordReader<LongWritable, FragmentWritable> createRecordReader(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
    {
    int nucWindow = 1000;
    return new FASTAFragmentRecordReader(nucWindow);
    }

  public static class FASTAFragmentRecordReader extends RecordReader<LongWritable, FragmentWritable>
    {
    private static final Log log = LogFactory.getLog(FASTAFragmentRecordReader.class);

    private int window;

    private LongWritable key = new LongWritable();
    private FragmentWritable value = new FragmentWritable();

    private CharacterReader reader;
    private FSDataInputStream inputStream;

    private static String header;
    private String splitChr;

    private long splitStart, splitEnd, headerLength;

    private long lastStart = 1;


    public FASTAFragmentRecordReader(int window)
      {
      this.window = window;
      }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      FileSplit split = (FileSplit) inputSplit;
      Path path = split.getPath();

      if (!org.lcsb.lu.igcsa.population.utils.FileUtils.FASTA_FILE.accept(null, path.toString()))
        throw new IOException(path.toString() + " is not a FASTA file.");

      splitChr = FileUtils.getChromosomeFromFASTA(path.toString());
      context.getConfiguration().set("chromosome", splitChr);

      splitStart = split.getStart();
      splitEnd = splitStart + split.getLength();

      CompressionCodecFactory factory = new CompressionCodecFactory(context.getConfiguration());
      CompressionCodec codec = factory.getCodec(path);

      inputStream = path.getFileSystem(context.getConfiguration()).open(path);

      if (codec != null)
        {
        CompressionInputStream is = codec.createInputStream(inputStream);
        reader = new CharacterReader(is, split.getStart());
        }
      else
        reader = new CharacterReader(inputStream, inputStream.getPos());

      // read the header
      if (splitStart == 0)
        {
        if (reader.read() == FASTAUtil.HEADER_IDENTIFIER)
          {
          // not doing anything with this right now, not sure there is anything to be done
          header = reader.readLine();
          headerLength = reader.getNumChars();
          }
        else  // this actually shouldn't happen but...you're at the beginning of a file that lacks the header?
          {
          reader.reset();
          log.warn("Start of stream (pos 0) in split but no header" + ((FileSplit) inputSplit).getStart());
          }
        }
      }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      if (splitEnd < inputStream.getPos())
        return false;

      // legacy...most of the code (I think) already is 1-based instead of 0-based so...
      long adjustedStart = (reader.getNumChars()-headerLength)+1;

      String fragment = reader.readCharacters(window);

      //log.info(splitChr + " " + adjustedStart);
      // no more data
      if (fragment == null)
        return false;

      long adjustedEnd = (adjustedStart) + fragment.length();

      long segment = (reader.getNumChars()-header.length())/window;
      if (fragment.length() < window)
        ++segment;

      key = new LongWritable( segment );
      value = new FragmentWritable(splitChr, adjustedStart, adjustedEnd, key.get(), fragment);

      return true;
      }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException
      {
      return key;
      }

    @Override
    public FragmentWritable getCurrentValue() throws IOException, InterruptedException
      {
      return value;
      }

    @Override
    public float getProgress() throws IOException, InterruptedException
      {
      float progress = 0.0f;
      if (inputStream.getPos() > 0)
        {
        progress = Math.min(1.0f, (float)inputStream.getPos()/splitEnd);
        }
      return progress;
      }

    @Override
    public void close() throws IOException
      {
      reader.close();
      inputStream.close();
      }

    }

  }
