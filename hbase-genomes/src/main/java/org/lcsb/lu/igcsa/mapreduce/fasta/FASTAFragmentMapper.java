/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.tables.ChromosomeResult;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;

import java.io.IOException;

public class FASTAFragmentMapper extends Mapper<LongWritable, FragmentWritable, LongWritable, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(FASTAFragmentMapper.class);

  private HBaseGenomeAdmin admin;
  private String genomeName;
  //private String chr;
  private HBaseGenome genome;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    super.setup(context);
    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());

    FileSplit fileSplit = (FileSplit) context.getInputSplit();
    String filePath = fileSplit.getPath().toString();

    genomeName = context.getConfiguration().get("genome");
    genome = this.admin.getGenome(genomeName);
    }

  @Override
  protected void map(LongWritable key, FragmentWritable fragment, Context context) throws IOException, InterruptedException
    {
    if (genome.getChromosome(fragment.getChr()) == null)
      genome.addChromosome(fragment.getChr(), 0, 0);

    if (genome.getChromosome(fragment.getChr()).getSequence(fragment.getSegment()) == null)
      {
      // pretty much just chopping the file up and spitting it back out into the HBase tables
      //FragmentWritable fragment = value;// FragmentWritable.read(value.get());
      genome.getChromosome(fragment.getChr()).addSequence(fragment.getStart(), fragment.getEnd(),
                                                                                    fragment.getSequence(), fragment.getSegment());
      ChromosomeResult incremented = this.admin.getChromosomeTable().incrementSize(ChromosomeRow.createRowId(genomeName,
                                                                                                             fragment.getChr()), 1, (fragment.getEnd() - fragment.getStart()));

      log.debug("Key:" + key + " " + fragment.toString() + "seg/length: " + incremented.getSegmentNumber() + "," + incremented.getLength());
      }
    else
      System.err.println(fragment.toString() + " already existed, skipping.");
    }


  }