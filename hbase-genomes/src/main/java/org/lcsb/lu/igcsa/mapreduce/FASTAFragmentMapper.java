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


import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;

import java.io.IOException;

public class FASTAFragmentMapper  extends Mapper<LongWritable, FragmentWritable, LongWritable, FragmentWritable>
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

    genomeName = context.getConfiguration().get("genome");
    chr = context.getConfiguration().get("chromosome");
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
    this.admin.getChromosomeTable().incrementSize(ChromosomeRow.createRowId(genomeName, chr), 1, (fragment.getEnd() - fragment.getStart()));
    /*
    In this case I don't need to write the keys/values, however using a custom writable I am getting a strange EOFException when hadoop tries to deserialize
    context.write(key, value);
    */
    log.info(key + " " + value.toString());
    }
  }