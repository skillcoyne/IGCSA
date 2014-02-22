/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.tables.ChromosomeResult;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.io.IOException;

public class FASTAFragmentMapper extends Mapper<LongWritable, FragmentWritable, LongWritable, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(FASTAFragmentMapper.class);

  private HBaseGenomeAdmin admin;
  private String genomeName;
  //private String chr;
  private HBaseGenome genome;
  private HBaseChromosome chromosome;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    super.setup(context);
    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());

    FileSplit fileSplit = (FileSplit) context.getInputSplit();
    Path filePath = fileSplit.getPath();

    genomeName = context.getConfiguration().get("genome");
    genome = this.admin.getGenome(genomeName);

    String chr = FileUtils.getChromosomeFromFASTA(filePath.getName());
    chromosome = genome.getChromosome(chr);
    if (chromosome == null)
      chromosome = genome.addChromosome(chr, 0, 0);
    }


    @Override
  protected void map(LongWritable key, FragmentWritable fragment, Context context) throws IOException, InterruptedException
    {
    // pretty much just chopping the file up and spitting it back out into the HBase tables
    boolean added = chromosome.addSequence(fragment.getStart(), fragment.getEnd(), fragment.getSequence(),
                                                                        fragment.getSegment(), false);
    if (added)
      this.admin.getChromosomeTable().incrementSize(ChromosomeRow.createRowId(genomeName, fragment.getChr()), 1,
                                                    (fragment.getEnd() - fragment.getStart()));
    else
      log.warn("Failed to add sequence " + fragment.getChr() + ": " + fragment.getSegment());
    }


  }