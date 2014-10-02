/**
 * org.lcsb.lu.igcsa.mapreduce.sam
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.sam;

import net.sf.samtools.SAMRecord;
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


public class ScoreReadsReducer extends Reducer<Text, SAMRecordWritable, Text, Text>
  {
  private static final Log log = LogFactory.getLog(ScoreReadsReducer.class);

  //private String[] names;
  private MultipleOutputs mos;
  private LinkedHashMap<String, String> outputNames;
  private List<String> keySetIndexList;


  private int baseQuals(SAMRecord record)
    {
    int qualSum = 0;
    for (byte b: record.getBaseQualities()) qualSum += b;
    return qualSum;
    }

  private String orientation(SAMRecord record)
    {
    String orient = "";
    orient = (record.getReadNegativeStrandFlag())? "R": "F";
    orient += (record.getMateNegativeStrandFlag())? "R": "F";
    return orient;
    }

  private void getEMMixtures(List<Double> data)
    {
    double[][] vv = new double[2][data.size()];
    double min = Collections.min(data);
    double max = Collections.max(data);
    double cutoff = (max-min)/2;




    }

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
  protected void reduce(Text key, Iterable<SAMRecordWritable> values, Context context) throws IOException, InterruptedException
    {
    ArrayList<Double> logDistances = new ArrayList<Double>();
    for (SAMRecordWritable smw: values)
      {
      SAMRecord record = smw.getSamRecord();
      logDistances.add(new org.apache.commons.math3.analysis.function.Log().value(record.getInferredInsertSize()));
      }



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
