/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;
import org.lcsb.lu.igcsa.mapreduce.NullReducer;
import org.lcsb.lu.igcsa.mapreduce.SelectedRowCount;

import java.io.IOException;

public class UpdateGenome extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(UpdateGenome.class);

  @Override
  public int run(String[] args) throws Exception
    {
//    if (args.length < 1)
//      throw new Exception("Missing argument: genome name.");


    String genome = "GRCh37";//args[0];

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    config.set("genome", genome);

    // get all sequences for the given genome and chromosome
    Scan chrScan = genomeAdmin.getSequenceTable().getScanFor(new Column("info", "genome", genome), new Column("loc", "chr", "1"));
    chrScan.setCaching(200);
    chrScan.setCacheBlocks(true);

    // set up the job
    Job job = new Job(config, "Mutated Genome Chromosome Update");
    job.setJarByClass(MutateFragments.class);

    job.setMapperClass(ChromosomeUpdateMapper.class);
    TableMapReduceUtil.initTableMapperJob(genomeAdmin.getSequenceTable().getTableName(), chrScan, ChromosomeUpdateMapper.class, Text.class, IntWritable.class, job);

    job.setReducerClass(ChromosomeUpdate.class);
    TableMapReduceUtil.initTableReducerJob(genomeAdmin.getChromosomeTable().getTableName(), ChromosomeUpdate.class, job);

    job.setNumReduceTasks(1);

    //job.waitForCompletion(true);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static class ChromosomeUpdate extends TableReducer<Text, IntWritable, Text>
    {
    private String genome;
    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      genome = context.getConfiguration().get("genome");
      }

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
      {
      // the number of segments will correspond to the number of values, each value is the length of a segment.
      int chrLength = 0, segments = 0;
      for(IntWritable iw: values)
        {
        chrLength += iw.get();
        ++segments;
        }
      String chr = key.toString();
      HBaseGenomeAdmin.getHBaseGenomeAdmin().getGenome(genome).updateChromosome(chr, chrLength, segments);
      }
    }

  public static class ChromosomeUpdateMapper extends TableMapper<Text, IntWritable>
    {
    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
      {
      // Keyed on the chromosome name log the length of each segment sequence
      SequenceResult result = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);
      context.write(new Text(result.getChr()), new IntWritable( (int)result.getSequenceLength()) );
      }


    }


  public static void main(String args[]) throws Exception
    {
    args = new String[]{"GRCh37"};
    if (args.length < 1)
      throw new Exception("Missing argument: genome name.");

    ToolRunner.run(new UpdateGenome(), args);
    }

  }
