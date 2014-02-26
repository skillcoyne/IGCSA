package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce.fasta
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TSVFragmentMapper extends Mapper<LongWritable, FragmentWritable, Text, LongWritable>
  {
  static Logger log = Logger.getLogger(TSVFragmentMapper.class.getName());

  private MultipleOutputs mos;
  private String genomeName;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    mos = new MultipleOutputs(context);

    FileSplit fileSplit = (FileSplit) context.getInputSplit();
    Path filePath = fileSplit.getPath();
    String chr = FileUtils.getChromosomeFromFASTA(filePath.getName());

    genomeName = context.getConfiguration().get("genome");
    }

  @Override
  protected void map(LongWritable key, FragmentWritable value, Context context) throws IOException, InterruptedException
    {
    String rowId = SequenceRow.createRowId(genomeName, value.getChr(), value.getSegment());

    Text out = new Text(genomeName + "\t" + value.getChr() + "\t" + value.getStart() + "\t" +
                        value.getEnd() + "\t" + value.getSegment() + "\t" + value.getSequence() );
    mos.write("sequence", new Text(rowId), out);

    context.write(new Text(value.getChr()), new LongWritable(value.getSegment()));
    }

  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException
    {
    mos.close();
    }
  }
