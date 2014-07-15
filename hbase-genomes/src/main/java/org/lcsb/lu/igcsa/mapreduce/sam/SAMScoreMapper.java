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
import java.util.ArrayList;
import java.util.List;


public class SAMScoreMapper extends Mapper<LongWritable, SAMRecordWritable, Text, IntWritable>
  {
  private static final Log log = LogFactory.getLog(SAMScoreMapper.class);

  private String name;
  private List<IntRange> alignmentLocations = new ArrayList<IntRange>();

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    name = ((FileSplit)context.getInputSplit()).getPath().getName();

    int sequenceLength = context.getConfiguration().getInt(name + "." + ScoreSAMJob.SEQ_LEN, 0);
    int bpLocation = context.getConfiguration().getInt(name + "." + ScoreSAMJob.BP_LOC, 0);

    if (sequenceLength <= 0 || bpLocation <= 0)
      throw new IOException("Missing sequence length or bp location in SAM file.");

    int leftSide = bpLocation/2;
    int rightSide = (sequenceLength - bpLocation)/2;
    int mid = sequenceLength/5;

    alignmentLocations.add(new IntRange(1, leftSide));
    alignmentLocations.add(new IntRange(1 + leftSide, bpLocation));
    alignmentLocations.add(new IntRange((bpLocation - mid), (bpLocation + mid)));
    alignmentLocations.add(new IntRange(bpLocation, (bpLocation + rightSide)));
    alignmentLocations.add(new IntRange((bpLocation + rightSide + 1), sequenceLength));
   }

  @Override
  protected void map(LongWritable key, SAMRecordWritable value, Context context) throws IOException, InterruptedException
    {
    SAMRecord record = value.getSamRecord();

    IntWritable count = new IntWritable(1);

    // total reads
    context.getCounter(ScoreSAMJob.SAM_COUNTERS.TOTAL).increment(1);

    // properly paired
    if (record.getProperPairFlag())
      context.getCounter(ScoreSAMJob.SAM_COUNTERS.PROPER_PAIRS).increment(1);

    // If paired and not a duplicate count it. Ignores MapQ
    if (record.getProperPairFlag() && record.getAlignmentStart() > 0 && !record.getDuplicateReadFlag())
      {
      for (IntRange ir: alignmentLocations)
        {
        if (ir.containsInteger(record.getAlignmentStart()))
          {
          switch (alignmentLocations.indexOf(ir))
            {
            case 0:
              context.write(new Text(name + ".leftb1"), count);
            case 1:
              context.write(new Text(name + ".leftb2"), count);
            case 2:
              context.write(new Text(name + ".bp"), count);
            case 3:
              context.write(new Text(name + ".rightb1"), count);
            case 4:
              context.write(new Text(name + ".rightb2"), count);
            }
          }
        }
      }
    }

  }
