package org.lcsb.lu.igcsa;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hbase.HBaseConfiguration;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.aws.AWSProperties;
import org.lcsb.lu.igcsa.aws.AWSUtils;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAFragmentInputFormat;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAFragmentMapper;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LoadFromFASTA extends JobIGCSA
  {
  static Logger log = Logger.getLogger(LoadFromFASTA.class.getName());

  private Collection<Path> paths;

  public LoadFromFASTA(String genomeName, Collection<Path> paths)
    {
    super(HBaseConfiguration.create());

    this.paths = paths;
    getConf().set("genome", genomeName);

    if (paths.iterator().next().toString().contains("s3"))
      setAWSProps();
    }

  private void setAWSProps()
    {
    AWSProperties props = AWSProperties.getProperties();
    getConf().set("fs.s3n.awsAccessKeyId", props.getAccessKey());
    getConf().set("fs.s3n.awsSecretAccessKey", props.getSecretKey());

    // might help with a strange problem in AWS
    log.info("Region handlers: " + getConf().get("hbase.regionserver.handler.count") + " increasing to 100");

    getConf().setInt("hbase.regionserver.handler.count", 100);
    getConf().setInt("hbase.rpc.timeout", 360000);

    log.info("hbase timeout set to: " + getConf().get("hbase.rpc.timeout"));
    }

  @Override
  public int run(String[] args) throws Exception
    {
    Job job = new Job(getConf(), "Reference Genome Fragmentation");

    job.setJarByClass(LoadFromFASTA.class);
    job.setMapperClass(FASTAFragmentMapper.class);

    job.setMapOutputKeyClass(LongWritable.class);
    job.setMapOutputValueClass(FragmentWritable.class);

    job.setInputFormatClass(FASTAFragmentInputFormat.class);

    for (Path path : paths)
      {
      log.info(path.toString());
      FileInputFormat.addInputPath(job, path);
      }

    // because we aren't emitting anything from mapper
    job.setNumReduceTasks(0);
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static void main(String[] args) throws Exception
    {
    GenericOptionsParser parser = new GenericOptionsParser(new Configuration(), args);

    args = parser.getRemainingArgs();
    if (args.length < 2)
      {
      System.err.println("Usage: LoadFromFASTA <genome name> <fasta directory>");
      System.exit(-1);
      }

    Configuration conf = parser.getConfiguration();//HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(HBaseConfiguration.create());

    String genomeName = args[0];
    String fastaDir = args[1];

    if (admin.getGenome(genomeName) != null)
      {
      System.out.println("Genome '" + genomeName + "' already exists, overwrites are not allowed. Deleting genome.");
      //System.exit(-1);
      admin.deleteGenome(genomeName);
      }

    HBaseGenome genome = admin.getGenome(genomeName);
    if (genome == null)
      genome = new HBaseGenome(genomeName, null);

    Collection<Path> filePaths = new ArrayList<Path>();
    if (fastaDir.startsWith("s3"))
      {
      Pattern s3pattern = Pattern.compile("^s3n?:\\/\\/(\\w+[-\\w+]*).*");
      Matcher s3file = s3pattern.matcher(fastaDir);
      if (!s3file.matches())
        throw new Exception("A S3 bucket name could not be determined from " + fastaDir);

      String bucket = s3file.group(1);
      Map<String, S3ObjectSummary> chromosomes = AWSUtils.listFASTAFiles(bucket);
      for (S3ObjectSummary s3Object : chromosomes.values())
        filePaths.add(new Path("s3n://" + s3Object.getBucketName() + "/" + s3Object.getKey()));
      }
    else //if (fastaDir.startsWith("hdfs"))
      {
      FileSystem fs = FileSystem.get(conf);
      FileStatus[] statuses = fs.listStatus(new Path(fastaDir), new PathFilter()
      {
      @Override
      public boolean accept(Path path)
        {
        return FileUtils.FASTA_FILE.accept(null, path.getName());
        }
      });
      for (FileStatus status : statuses)
        filePaths.add(status.getPath());
      }

    ToolRunner.run(new LoadFromFASTA(genomeName, filePaths), null);
    }

  }




