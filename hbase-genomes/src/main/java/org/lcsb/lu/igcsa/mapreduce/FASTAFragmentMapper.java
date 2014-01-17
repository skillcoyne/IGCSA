/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;


import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.tables.ChromosomeResult;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FASTAFragmentMapper extends Mapper<LongWritable, FragmentWritable, LongWritable, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(FASTAFragmentMapper.class);

  private HBaseGenomeAdmin admin;
  private String genomeName;
  private String chr;


    @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    super.setup(context);
    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());

    FileSplit fileSplit = (FileSplit) context.getInputSplit();
    String filePath = fileSplit.getPath().toString();

    log.info("CREATING MAPPER FOR: " + filePath);

    genomeName = context.getConfiguration().get("genome");
    chr = FileUtils.getChromosomeFromFASTA(filePath);

    log.info(chr);

    if (admin.getGenome(genomeName).getChromosome(chr) == null)
      admin.getGenome(genomeName).addChromosome(chr, 0, 0);
    }

  @Override
  protected void map(LongWritable key, FragmentWritable value, Context context) throws IOException, InterruptedException
    {
    // pretty much just chopping the file up and spitting it back out into the HBase tables
    FragmentWritable fragment = value;// FragmentWritable.read(value.get());
    this.admin.getGenome(genomeName).getChromosome(chr).addSequence(
        fragment.getStart(),
        fragment.getEnd(),
        fragment.getSequence(),
        fragment.getSegment());

    ChromosomeResult incremented = this.admin.getChromosomeTable().incrementSize(ChromosomeRow.createRowId(genomeName, chr), 1, (fragment.getEnd() - fragment.getStart()));

    log.info("Key:" + key + " " + value.toString() + "seg/length: " + incremented.getSegmentNumber() + "," + incremented.getLength());
    }
  }