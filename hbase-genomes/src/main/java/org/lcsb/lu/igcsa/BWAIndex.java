/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import com.m6d.filecrush.crush.Crush;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.Tool;

import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FASTAUtil;
import org.lcsb.lu.igcsa.mapreduce.NullReducer;

import java.io.File;
import java.io.IOException;


public class BWAIndex extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(BWAIndex.class);

  private Configuration conf;
  private Path fasta;


  public static void main(String[] args) throws Exception
    {
    new BWAIndex().run(args);


    }

  public int run(String[] args) throws Exception
    {
    this.fasta = new Path(args[0]);
    String bwa = args[1];
    this.conf = new Configuration();

    Path fasta = new Path(args[0]);
    Configuration conf = new Configuration();

    log.info(fasta.getParent());

    log.info(fasta.getName());

    FileSystem fs = fasta.getFileSystem(conf);
    Path fullFasta = null;
    for(FileStatus status: fs.listStatus( new Path(fasta, fasta.getName()+".fa")))
      {
      fullFasta = status.getPath();
      log.info( status.getPath() );
      }

    if (fullFasta == null)
      throw new Exception(new Path(fasta, fasta.getName()+".fa").toString() + " doesn't exist. Exiting.");

    log.info(fs.getCanonicalServiceName());

    // stick bwa in hdfs
//    fs.mkdirs(new Path("/tmp/bwa-tools"));
//    FileUtil.copy(new File(bwa), fs, new Path("/tmp/bwa-tools/bwa"), false, conf);

    String bwaCmd = "tmp/bwa-tools/bwa index " + fullFasta.toString();
    log.info(bwaCmd);
    Process p = Runtime.getRuntime().exec( bwaCmd );

return 0;

//    Job job = new Job(conf, "Index FASTA files");
//    job.setJarByClass(BWAIndex.class);
//
//    job.setMapperClass(FASTAMapper.class);
//
//    job.setOutputKeyClass(Text.class);
//    job.setOutputValueClass(Text.class);
//
//    job.setReducerClass(NullReducer.class);
//
//    job.setInputFormatClass(FASTAInputFormat.class);
//    FileInputFormat.addInputPath(job, fasta);
//
//    job.setOutputFormatClass(NullOutputFormat.class);
//
//    return (job.waitForCompletion(true) ? 0 : 1);
    }

  static class FASTAMapper extends Mapper<Text, Text, Text, Text>
    {

    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      }

    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException
      {
      log.info(key);
      log.info(value);

      super.map(key, value, context);
      }
    }


  }
