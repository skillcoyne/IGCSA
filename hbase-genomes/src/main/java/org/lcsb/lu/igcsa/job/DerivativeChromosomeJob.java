/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.lcsb.lu.igcsa.generators.GenerateFullKaryotype;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.mapreduce.*;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil.deleteChecksumFiles;


public class DerivativeChromosomeJob extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(DerivativeChromosomeJob.class);

  private Scan scan;
  private Path output;
  private List<Location> filterLocations;
  private FASTAHeader header;
  private Aberration aberration;
  private String jobId;

  public DerivativeChromosomeJob(Configuration conf, Scan scan, Path output, List<Location> filterLocations, Aberration aberration, FASTAHeader header)
    {
    super(conf);
    this.scan = scan;
    this.output = output;
    this.filterLocations = filterLocations;
    this.header = header;
    this.aberration = aberration;
    }


  public Job createJob(String[] args) throws IOException
    {
    Job job = new Job(getConf(), "Generate derivative FASTA: " + aberration.getFASTAName());
    job.setJarByClass(GenerateFullKaryotype.class);

    job.setSpeculativeExecution(false);
    job.setReduceSpeculativeExecution(false);

    // M/R setup
    job.setMapperClass(SequenceRequestMapper.class);
    SequenceRequestMapper.setLocations(job, filterLocations);
    if (aberration.getAberration().equals(AberrationTypes.INVERSION)) SequenceRequestMapper.setLocationsToReverse(job, filterLocations.get(1));

    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().getTableName(), scan,
        SequenceRequestMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    // custom partitioner to make sure the segments go to the correct reducer sorted
    job.setPartitionerClass(FragmentPartitioner.class);

    job.setReducerClass(SequenceFragmentReducer.class);
    job.setNumReduceTasks(filterLocations.size()); // one reducer for each segment

    // Output format setup
    job.setOutputFormatClass(NullOutputFormat.class);
    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 70);
    FASTAOutputFormat.addHeader(job, header);

    if (aberration.getAberration().equals(AberrationTypes.ISOCENTRIC))
      {
      if (aberration.getBands().get(0).whichArm().equals("q"))
        IsoChromosomeMapper.reverseFirst(true, job);

      job.setMapperClass(IsoChromosomeMapper.class);
      for (int i=0; i<2; i++)
        MultipleOutputs.addNamedOutput(job, Integer.toString(i), FASTAOutputFormat.class, LongWritable.class, Text.class);
      }
    else
      {
      for (int order = 0; order < filterLocations.size(); order++)
        MultipleOutputs.addNamedOutput(job, Integer.toString(order), FASTAOutputFormat.class, LongWritable.class, Text.class);
      }
    return job;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    Job job = createJob(args);
    return (job.waitForCompletion(true) ? 0 : 1);
    }


  // just to clean up the main method a bit
  public Path mergeOutputs(AberrationTypes abrType, Path output, int numLocs) throws Exception
    {
    FileSystem jobFS = this.getJobFileSystem(output.toUri());
    // CRC files mess up any attempt to directly read/write from an unchanged file which means copying/moving fails too. Easiest fix
    // right now is to dump the file.
    deleteChecksumFiles(jobFS, output);
    /*
  We now have output files.  In most cases the middle file(s) will be the aberration sequences.
  In many cases they can just be concatenated as is. Exceptions:
    - duplication: the middle file needs to be duplicated before concatenation
    - iso: there should be only 1 file, it needs to be duplicated in reverse before concatenation
    */
    log.info(jobFS.getWorkingDirectory());
    if (abrType.getCytogeneticDesignation().equals("dup"))
      {
      log.info("DUP aberration");
      if (numLocs > 3) throw new RuntimeException("This should not happen: dup has more than 3 locations");

      // move subsequent files
      for (int i = numLocs - 1; i > 1; i--)
        {
        // move files
        FileUtil.copy(jobFS, new Path(output, Integer.toString(i)), jobFS, new Path(output, Integer.toString(i + 1)), true, false,
                      jobFS.getConf());
        }
      //then copy the duplicated segment
      FileUtil.copy(jobFS, new Path(output, Integer.toString(1)), jobFS, new Path(output, Integer.toString(2)), false, false,
                    jobFS.getConf());
      }

    if (abrType.getCytogeneticDesignation().equals("iso"))
      {
      log.info("ISO aberration");
      if (numLocs > 2) throw new RuntimeException("This should not happen: iso has more than 2 locations");
      }


    // create merged FASTA at chromosome level -- there is an issue here that it just concatenates the files which means at the merge points there are strings of different lengths.  This is an issue in samtools.

    Path tmp = new Path("/tmp/" + String.valueOf(new Random(System.currentTimeMillis()).nextLong()), output.getName() + ".fa");
    log.info("Temp path " + tmp.toString());
    Path newOutput = new Path(output, tmp.getName());
    log.info("New path " + newOutput.toString());

    if (FileUtil.copyMerge(jobFS, output, jobFS, tmp, true, getConf(), ""))
      FileUtil.copy(jobFS, tmp, jobFS, newOutput, true, true, getConf());
    else
      throw new IOException("Failed to merge files in " + output);
    jobFS.delete(tmp.getParent(), true);

    return newOutput;
    }


  }
