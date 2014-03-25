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
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.mapreduce.*;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;

import java.util.List;


public class DerivativeChromosomeJob extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(DerivativeChromosomeJob.class);

  private Scan scan;
  private Path output;
  private List<Location> filterLocations;
  private String aberrationType;
  private FASTAHeader header;

  public DerivativeChromosomeJob(Configuration conf, Scan scan, Path output, List<Location> filterLocations, String aberrationType, FASTAHeader header)
    {
    super(conf);
    this.scan = scan;
    this.output = output;
    this.filterLocations = filterLocations;
    this.aberrationType = aberrationType;
    this.header = header;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    Job job = new Job(getConf(), "Generate derivative FASTA");
    job.setJarByClass(GenerateDerivativeChromosomes.class);

    // M/R setup
    job.setMapperClass(SequenceRequestMapper.class);
    SequenceRequestMapper.setLocations(job, filterLocations);
    if (aberrationType.equals("inv"))
      SequenceRequestMapper.setLocationsToReverse(job, filterLocations.get(1));

    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().getTableName(), scan, SequenceRequestMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    // custom partitioner to make sure the segments go to the correct reducer sorted
    job.setPartitionerClass(FragmentPartitioner.class);

    job.setReducerClass(SequenceFragmentReducer.class);
    job.setNumReduceTasks(filterLocations.size()); // one reducer for each segment

    // Output format setup
    job.setOutputFormatClass(NullOutputFormat.class);
    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 70);
    FASTAOutputFormat.addHeader(job, new Path(output, "0"), header);
    for (int order = 0; order < filterLocations.size(); order++)
      MultipleOutputs.addNamedOutput(job, Integer.toString(order), FASTAOutputFormat.class, LongWritable.class, Text.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }
  }
