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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.io.IOException;


// Returns entire file
public class FASTAInputFormat extends FileInputFormat<Text, Text>
  {
  private static final Log log = LogFactory.getLog(FASTAInputFormat.class);

  @Override
  protected boolean isSplitable(JobContext context, Path filename)
    {
    // force the entire file to be read in a single split. This is required for BWA index
    return false;
    }

  public RecordReader<Text, Text> createRecordReader(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
    {
    return new FASTAFileNameRecordReader();
    }

  public static class FASTAFileNameRecordReader extends RecordReader<Text, Text>
    {
    private Text key;
    private Text value;
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      FileSplit split = (FileSplit) inputSplit;
      Path path = split.getPath();
      key = new Text(FileUtils.getChromosomeFromFASTA(path.toString()));
      value = new Text(path.toString());
      }

    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      return true;
      }

    public Text getCurrentKey() throws IOException, InterruptedException
      {
      return key;
      }

    public Text getCurrentValue() throws IOException, InterruptedException
      {
      return value;
      }

    public float getProgress() throws IOException, InterruptedException
      {
      return 0;
      }

    public void close() throws IOException
      {
      }
    }




  public static class FASTARecordReader extends RecordReader<Text, Text>
    {
    private static final Log log = LogFactory.getLog(FASTARecordReader.class);

    private Text key;
    private Text value;

    private CharacterReader reader;
    private FSDataInputStream inputStream;

    private long splitStart;
    private long splitEnd;
    private String splitChr;

    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException
      {
      FileSplit split = (FileSplit) inputSplit;
      Path path = split.getPath();

      if (!org.lcsb.lu.igcsa.utils.FileUtils.FASTA_FILE.accept(null, path.toString()))
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
      }

    public boolean nextKeyValue() throws IOException, InterruptedException
      {
      if (splitEnd < inputStream.getPos())
        return false;

      key = new Text(splitChr);
      value = new Text();
      String line;
      while ( (line = reader.readLine()) != null)
        {
        if (line.startsWith( String.valueOf(FASTAUtil.HEADER_IDENTIFIER)) )
          value.append( (line+FASTAUtil.CARRIAGE_RETURN).getBytes(), 0, line.length()+1 );
        else
          value.append( line.getBytes(), 0, line.length() );
        }

      return true;
      }

    public Text getCurrentKey() throws IOException, InterruptedException
      {
      return key;
      }

    public Text getCurrentValue() throws IOException, InterruptedException
      {
      return value;
      }

    public float getProgress() throws IOException, InterruptedException
      {
      return inputStream.getPos();
      }

    public void close() throws IOException
      {
      reader.close();
      }
    }


  }
