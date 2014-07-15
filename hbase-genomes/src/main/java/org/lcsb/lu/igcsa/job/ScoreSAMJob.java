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
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.sam.SAMInputFormat;
import org.lcsb.lu.igcsa.mapreduce.sam.SAMScoreMapper;

import java.io.IOException;


public class ScoreSAMJob extends JobIGCSA
  {
  public static final String INPUT_NAMES = "input.sam.files";
  public static final String SEQ_LEN = "sam.sequence.length";
  public static final String BP_LOC = "sam.bp.location";

  public static final String LEFT  = "seq.band.left";
  public static final String RIGHT  = "seq.band.right";
  private Job job;


  public static enum SAM_COUNTERS {
    TOTAL,
    PROPER_PAIRS
  }

  private static final Log log = LogFactory.getLog(ScoreSAMJob.class);
  private Path outputPath;

  public static void main(String[] args) throws Exception
    {
    ScoreSAMJob ss = new ScoreSAMJob();
    ToolRunner.run(ss, new String[]{"-p", "/tmp/sam", "-o", "/tmp/scoring"});

    log.info(ss.getProperPairRatio());
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

  public double getProperPairRatio() throws IOException
    {
    long total = job.getCounters().findCounter(SAM_COUNTERS.TOTAL).getValue();
    long pp =  job.getCounters().findCounter(SAM_COUNTERS.PROPER_PAIRS).getValue();

    return (double)pp/total;
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
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs(), this.getClass());

    Path inputPath = new Path(cl.getOptionValue("p"));
    outputPath = new Path(cl.getOptionValue("o"));

    FileSystem fs = FileSystem.get(outputPath.toUri(), getConf());
    fs.delete(outputPath, true);

    Job job = new Job(getConf(), "Score SAM " + inputPath.toString());
    job.setJarByClass(ScoreSAMJob.class);

    job.setMapperClass(SAMScoreMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setInputFormatClass(SAMInputFormat.class);
    FileInputFormat.addInputPath(job, inputPath);
    SAMInputFormat.getSAMHeaderInformation(job, inputPath);

    job.setReducerClass(ScoreReadsReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    job.setOutputFormatClass(TextOutputFormat.class);
    TextOutputFormat.setOutputPath(job, outputPath);

    return job;
    }


  static class ScoreReadsReducer extends Reducer<Text, IntWritable, Text, Text>
    {
    private String[] names;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      names = context.getConfiguration().getStrings(ScoreSAMJob.INPUT_NAMES);

      }

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
      {
      String currentName = "";
      for (String name: names)
        {
        if (key.toString().contains(name))
          {
          currentName = name;
          break;
          }
        }

      int length = 0;
      if (key.toString().contains("bp"))
        length = context.getConfiguration().getInt(currentName + "." + ScoreSAMJob.BP_LOC, 0)/5;
      else if (key.toString().contains("left"))
        length = context.getConfiguration().getInt(currentName + "." + ScoreSAMJob.LEFT, 0);
      else
        length = context.getConfiguration().getInt(currentName + "." + ScoreSAMJob.RIGHT, 0);


      int sum = 0;
      for (IntWritable iw: values)
        sum += iw.get();

      Text value = new Text( sum + "\t" + length);
      context.write(key, value);
      //context.write(key, new IntWritable(sum));
      }
    }



  }
