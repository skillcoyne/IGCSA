/**
 * org.lcsb.lu.igcsa.job
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.sam.SAMInputFormat;
import org.lcsb.lu.igcsa.mapreduce.sam.SAMScoreMapper;
import org.lcsb.lu.igcsa.mapreduce.sam.ScoreReadsReducer;
import sun.dc.pr.PathFiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ScoreBandRatios extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(ScoreBandRatios.class);

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new ScoreBandRatios(new Configuration()), null);
    }

  public ScoreBandRatios(Configuration conf)
    {
    super(conf);
    }

  public Job createJob(String[] args) throws Exception
    {
    Path inputPath = new Path(args[0]);
    Job job = new Job(getConf(), "Calculate band ratios");
    job.setJarByClass(ScoreBandRatios.class);

    job.setMapperClass(BandRatiosMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);

    job.setInputFormatClass(TextInputFormat.class);
    TextInputFormat.addInputPath(job, inputPath);

    job.setNumReduceTasks(0);

    Path output = new Path(inputPath.getParent(), "ratios");
    FileSystem.get(output.toUri(), getConf()).delete(output, true);

    job.setOutputFormatClass(TextOutputFormat.class);
    TextOutputFormat.setOutputPath(job, output);

    return job;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    Job job = createJob(args);
    return (job.waitForCompletion(true) ? 0 : 1);
    }

  static class BandRatiosMapper extends Mapper<LongWritable, Text, Text, Text>
    {
    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
      {
      FileSystem fs = FileSystem.get(context.getWorkingDirectory().toUri(), context.getConfiguration());

      final Path p = new Path(value.toString());
      FileStatus[] statuses = fs.listStatus(p.getParent(), new PathFilter()
      {
      @Override
      public boolean accept(Path path)
        {
        if (path.getName().matches(p.getName() + "-.*") || path.getName().equals(p.getName()))
          return true;
        return false;
        }
      });

      if (statuses.length <=0)
        throw new IOException("No paths at " + p.getParent() + " matching " + p.getName());


      // Read in the values
      FSDataInputStream is = fs.open(statuses[0].getPath());
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      // should only ever have 3
      List<String> lines = new ArrayList<String>();
      String line;
      while ( (line = reader.readLine()) != null)
        lines.add(line);

      if (lines.size() > 5)
        throw new IOException(lines.size() + " read from file " + statuses[0].getPath().toString() + " expecting 5!");


      double right = 0.0, left = 0.0, properPair = 0.0, totalReads = 0.0;

      for (String count: lines)
        {
        String[] cols = count.split("\t");
        double c = Double.parseDouble(cols[2]);
        switch ( COUNT.valueOf(cols[1]) )
          {
          case PP: properPair = c;
          case TOTAL: totalReads = c;
          }
        // didn't work to use the enum for these, probably a upper/lower case issue
        if (cols[1].equals("left"))
          left = c;
        if (cols[1].equals("right"))
          right = c;
        }

      double ratio = Math.abs( Math.log(left/right) );
      double rank = rank(totalReads, properPair);

      Text newKey = new Text( "[name,LR,RANK]\t" +  lines.get(0).split("\t")[0]);
      Text newValue = new Text( Double.toString(ratio) + "\t" + Double.toString(rank) );
      if (rank <= 0)
        newValue = new Text( "NA\t" + Double.toString(rank) );

      context.write(newKey, newValue);
      }
    }

  private static double rank(double total, double proper)
    {
    double ratio = proper/total;

    double rank = (double)Math.round(ratio);
    int digits = 10;
    while (rank <= 0)
      {
      rank = (double)Math.round(ratio * digits)/digits;
      digits = digits * 10;
      }
    return rank;
    }


  static enum COUNT
    {
    PP, TOTAL, left, mid, right;
    }

  }
