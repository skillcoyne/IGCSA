package org.lcsb.lu.igcsa;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hbase.HBaseConfiguration;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
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

import java.io.File;
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
public class LoadFromFASTA extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(LoadFromFASTA.class.getName());

  private static Configuration config;
  private String genomeName;

  private Collection<Path> paths;

  private Path path;

  public LoadFromFASTA(String genomeName, Collection<Path> paths)
    {
    config = HBaseConfiguration.create();
    this.genomeName = genomeName;
    this.paths = paths;

    if (paths.iterator().next().toString().contains("s3"))
      {
      AWSProperties props = AWSProperties.getProperties();
      config.set("fs.s3n.awsAccessKeyId", props.getAccessKey());
      config.set("fs.s3n.awsSecretAccessKey", props.getSecretKey());
      }
    }


  @Override
  public int run(String[] args) throws Exception
    {
    config.set("genome", genomeName);

    Job job = new Job(config, "Reference Genome Fragmentation");

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
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static void main(String[] args) throws Exception
    {
    //args = new String[]{"test", "hdfs://FASTA"};
    if (args.length < 2)
      {
      System.err.println("Usage: LoadFromFASTA <genome name> <fasta directory>");
      System.exit(-1);
      }

    Configuration conf = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);
    //admin.createTables();

    String genomeName = args[0];
    String fastaDir = args[1];

    admin.deleteGenome(genomeName);

    HBaseGenome genome = admin.getGenome(genomeName);
    if (genome == null)
      genome = new HBaseGenome(genomeName, null);

    Collection<Path> filePaths = new ArrayList<Path>();
    if (fastaDir.startsWith("s3"))
      {
      Pattern s3pattern = Pattern.compile("^s3n?:\\/\\/(\\w+[-\\w+]*).*");
      Matcher s3file = s3pattern.matcher(fastaDir);
      if (!s3file.matches()) throw new Exception("A S3 bucket name could not be determined from " + fastaDir);

      String bucket = s3file.group(1);
      Map<String, S3ObjectSummary> chromosomes = AWSUtils.listFASTAFiles(bucket);
      for (S3ObjectSummary s3Object : chromosomes.values())
        filePaths.add(new Path("s3n://" + s3Object.getBucketName() + "/" + s3Object.getKey()));
      }
    else if (fastaDir.startsWith("hdfs"))
      {
      FileSystem fs = new Path(fastaDir).getFileSystem(conf);
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
    else // local files
      {
      Map<String, File> files = org.lcsb.lu.igcsa.utils.FileUtils.getFASTAFiles(new File(fastaDir));
      for (File file: files.values())
        filePaths.add( new Path(file.getPath()) );
      }

    final long startTime = System.currentTimeMillis();
    ToolRunner.run(new LoadFromFASTA(genomeName, filePaths), null);
    final long elapsedTime = System.currentTimeMillis() - startTime;
    log.info("Finished job " + elapsedTime / 1000 + " seconds");
    }

  }




