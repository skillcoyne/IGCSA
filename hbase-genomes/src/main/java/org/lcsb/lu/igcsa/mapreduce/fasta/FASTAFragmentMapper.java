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
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.io.IOException;

public class FASTAFragmentMapper extends Mapper<LongWritable, FragmentWritable, LongWritable, FragmentWritable>
  {
  private static final Log log = LogFactory.getLog(FASTAFragmentMapper.class);

  private HBaseGenomeAdmin admin;
  private ChromosomeResult chromosome;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    super.setup(context);
    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());

    FileSplit fileSplit = (FileSplit) context.getInputSplit();
    Path filePath = fileSplit.getPath();
    String chr = FileUtils.getChromosomeFromFASTA(filePath.getName());

    String genomeName = context.getConfiguration().get("genome");
    GenomeResult genome = admin.getGenomeTable().getGenome(genomeName);

    chromosome = admin.getChromosomeTable().queryTable(ChromosomeRow.createRowId(genomeName, chr));
    if (chromosome == null)
      {
      String chromosomeRowId = admin.getChromosomeTable().addChromosome(genome, chr, 0, 0);
      chromosome = admin.getChromosomeTable().queryTable(chromosomeRowId);
      }

    }

  // pretty much just chopping the file up and spitting it back out into the HBase tables
  @Override
  protected void map(LongWritable key, FragmentWritable fragment, Context context) throws IOException, InterruptedException
    {
    long ts = System.currentTimeMillis();

    String seqRowId = admin.getSequenceTable().addSequence(chromosome, fragment.getStart(), fragment.getEnd(), fragment.getSequence(), fragment.getSegment());

    if (seqRowId != null)
      admin.getChromosomeTable().increment(chromosome.getRowId(), 1, (fragment.getEnd() - fragment.getStart()));
    else
      log.warn("Failed to add sequence " + fragment.getChr() + ": " + fragment.getSegment());

    log.info((System.currentTimeMillis() - ts) + " s");
    }


  }