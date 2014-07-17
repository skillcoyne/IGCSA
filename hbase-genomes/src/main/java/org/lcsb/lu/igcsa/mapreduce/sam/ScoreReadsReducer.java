/**
 * org.lcsb.lu.igcsa.mapreduce.sam
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.sam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.lcsb.lu.igcsa.job.ScoreSAMJob;

import java.io.IOException;
import java.util.*;


public class ScoreReadsReducer extends Reducer<Text, IntWritable, Text, Text>
  {
  private static final Log log = LogFactory.getLog(ScoreReadsReducer.class);

  //private String[] names;
  private MultipleOutputs mos;
  private LinkedHashMap<String, String> outputNames;
  private List<String> keySetIndexList;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    mos = new MultipleOutputs(context);
    outputNames = new LinkedHashMap<String, String>();

    String[] names = context.getConfiguration().getStrings(ScoreSAMJob.INPUT_NAMES);
    for (String input:names)
      {
      Path p = new Path(input);
      outputNames.put(input, p.getParent().getName().replace("-", ""));
      }

    keySetIndexList = new ArrayList<String>(outputNames.keySet());
    }

  @Override
  protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
    {
    String currentName = "";
    int index = -1;

    for (int i=0; i<keySetIndexList.size(); i++)
      {
      String seqName = context.getConfiguration().get(i + "." + ScoreSAMJob.SEQ_NAME);
      if (key.toString().contains(seqName))
        {
        index = i;
        currentName = seqName;
        break;
        }
      }

    int length = 1;
    String keySide = key.toString().replace(currentName + "\t", "");
    if (keySide.equals("mid"))
      length = (context.getConfiguration().getInt(index + "." + ScoreSAMJob.BP_LOC, 0)/5);
    else if (keySide.contains("left"))
      length = (context.getConfiguration().getInt(index + "." + ScoreSAMJob.LEFT, 0));
    else if (keySide.contains("right"))
      length = (context.getConfiguration().getInt(index + "." + ScoreSAMJob.RIGHT, 0));

    int sum = 0;
    for (IntWritable iw: values)
      sum += iw.get();

    double adjustedScores = (double)sum/(double)length;
    //Text value = new Text( sum + "\t" + length);
    Text value = new Text( Double.toString(adjustedScores));
    //context.write(key, value);
    String outputKey = outputNames.get(keySetIndexList.get(index));
    mos.write( outputKey, key, value );
    }

  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException
    {
    Path outputPath = new Path(context.getConfiguration().get("mapred.output.dir"));
    FileSystem fs = FileSystem.get(outputPath.toUri(), context.getConfiguration());

    FSDataOutputStream os = fs.create(new Path(outputPath, "score-files.txt"), true);
    for (String key: outputNames.keySet())
      os.write( (new Path(outputPath, outputNames.get(key)).toString() + "\n").getBytes() );
    os.flush();
    os.close();

    mos.close();
    }

  }
