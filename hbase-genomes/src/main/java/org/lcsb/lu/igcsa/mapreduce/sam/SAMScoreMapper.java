/**
 * org.lcsb.lu.igcsa.mapreduce.sam
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.sam;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.lcsb.lu.igcsa.job.ScoreSAMJob;

import java.io.IOException;
import java.util.*;


public class SAMScoreMapper extends Mapper<LongWritable, SAMRecordWritable, Text, IntWritable>
  {
  private static final Log log = LogFactory.getLog(SAMScoreMapper.class);

  private String seqName;
  private Map<IntRange, String> alignmentLocations = new HashMap<IntRange, String>();

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    String name = ((FileSplit)context.getInputSplit()).getPath().toString();

    int index = -1;
    String[] allInputs = context.getConfiguration().getStrings(ScoreSAMJob.INPUT_NAMES);
    for (int i=0; i<allInputs.length; i++)
      {
      if (allInputs[i].equals(name))
        { index = i; break; }
      }
    if (index < 0)
      throw new IOException(name + " is not in the list of inputs: " + Arrays.toString(allInputs));

    seqName = context.getConfiguration().get(index + "." + ScoreSAMJob.SEQ_NAME);
    int sequenceLength = context.getConfiguration().getInt(index + "." + ScoreSAMJob.SEQ_LEN, 0);
    int bpLocation = context.getConfiguration().getInt(index + "." + ScoreSAMJob.BP_LOC, 0);

    if (sequenceLength <= 0 || bpLocation <= 0)
      throw new IOException("Missing sequence length or bp location in SAM file.");

    int mid = sequenceLength/5;
    alignmentLocations.put(new IntRange(1, bpLocation), "left");
    alignmentLocations.put(new IntRange((bpLocation - mid), (bpLocation + mid)), "mid");
    alignmentLocations.put(new IntRange(bpLocation + 1, sequenceLength), "right");
   }

  @Override
  protected void map(LongWritable key, SAMRecordWritable value, Context context) throws IOException, InterruptedException
    {
    SAMRecord record = value.getSamRecord();

    IntWritable count = new IntWritable(1);
    IntWritable zero = new IntWritable(0);

    // total reads
    context.write(new Text(seqName + "\tTOTAL"), count);
    // properly paired
    if (record.getProperPairFlag())
      {
      context.write(new Text(seqName + "\tPP"), count);
      }
    else
      context.write(new Text(seqName+"\tPP"), zero);


    // If paired and not a duplicate count it. Ignores MapQ
    if (record.getProperPairFlag() && record.getAlignmentStart() > 0 && !record.getDuplicateReadFlag())
      {
      for (IntRange ir: alignmentLocations.keySet())
        {
        if (ir.containsInteger(record.getAlignmentStart()))
          context.write(new Text(seqName + "\t" + alignmentLocations.get(ir)), count);

        }
      }
    }

  }
