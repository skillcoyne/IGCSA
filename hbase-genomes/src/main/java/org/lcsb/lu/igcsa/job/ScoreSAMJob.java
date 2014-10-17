/**
 * org.lcsb.lu.igcsa.mapreduce.pipeline.scores
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.sam.MiniSAMScoreMapper;
import org.lcsb.lu.igcsa.mapreduce.sam.SAMInputFormat;
import org.lcsb.lu.igcsa.mapreduce.sam.ScoreReadsReducer;

import java.io.IOException;
import java.util.Arrays;


public class ScoreSAMJob extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(ScoreSAMJob.class);

  public static final String SEQ_NAME = "sam.sequence.name";
  public static final String INPUT_NAMES = "input.sam.files";
  public static final String SEQ_LEN = "sam.sequence.length";
  public static final String BP_LOC = "sam.bp.location";

  public static final String LEFT  = "seq.band.left";
  public static final String RIGHT  = "seq.band.right";

  private Job job;
  private Path inputPath, outputPath;

  public static void main(String[] args) throws Exception
    {
    ScoreSAMJob ss = new ScoreSAMJob();
    //ToolRunner.run(ss, new String[]{"-p", "/tmp/sam", "-o", "/tmp/scoring"});
    ToolRunner.run(ss, args);
    }

  public ScoreSAMJob()
    {
    super(new Configuration());

    Option path = new Option("p", "path", true, "Path to SAM files");
    path.setRequired(true);
    this.addOptions(path);

    path = new Option("o", "output", true, "Output path");
    path.setRequired(true);
    this.addOptions(path);
    }

  public Path getOutputPath()
    {
    log.info(outputPath.toString());
    return outputPath;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    job = createJob(args);
    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public Job createJob(String[] args) throws ParseException, IOException
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parseOptions(gop.getRemainingArgs(), this.getClass());

    log.info(Arrays.toString(gop.getRemainingArgs()));

    inputPath = new Path(cl.getOptionValue("p"));
    outputPath = new Path(cl.getOptionValue("o"));

    FileSystem fs = FileSystem.get(outputPath.toUri(), getConf());
    fs.delete(outputPath, true);

    Job job = new Job(getConf(), "Score SAM " + inputPath.toString());
    job.setJarByClass(ScoreSAMJob.class);

    job.setMapperClass(MiniSAMScoreMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setInputFormatClass(SAMInputFormat.class);
    String[] samFiles = SAMInputFormat.addSAMInputs(job, inputPath);
    log.info("Added sam files: " + Arrays.toString(samFiles));

    job.setReducerClass(ScoreReadsReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    job.setOutputFormatClass(TextOutputFormat.class);
    TextOutputFormat.setOutputPath(job, outputPath);

    for (String sam:samFiles)
      {
      Path p = new Path(sam);
      MultipleOutputs.addNamedOutput(job, p.getParent().getName().replace("-", ""), TextOutputFormat.class, Text.class, Text.class);
      }

    MultipleOutputs.addNamedOutput(job, "scores", TextOutputFormat.class, Text.class, Text.class);

    return job;
    }




  }
