package org.lcsb.lu.igcsa;

import com.amazonaws.services.s3.model.S3Object;
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
import org.lcsb.lu.igcsa.mapreduce.FASTAFragmentMapper;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.io.File;
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
  private String chr;
  private Path path;

  private LoadFromFASTA(String genomeName, String chr)
    {
    config = HBaseConfiguration.create();
    this.genomeName = genomeName;
    this.chr = chr;
    }

  public LoadFromFASTA(String genomeName, String chr, Path path)
    {
    this(genomeName, chr);
    this.path = path;
    }

  public LoadFromFASTA(String genomeName, String chr, File file)
    {
    this(genomeName, chr);
    path = new Path(file.getAbsolutePath());
    }

  public LoadFromFASTA(String genomeName, String chr, S3Object s3Object)
    {
    this(genomeName, chr);

    AWSProperties props = AWSProperties.getProperties();
    config.set("fs.s3n.awsAccessKeyId", props.getAccessKey());
    config.set("fs.s3n.awsSecretAccessKey", props.getSecretKey());
    path = new Path("s3n://" + s3Object.getBucketName() + "/" + s3Object.getKey());
    }

  @Override
  public int run(String[] args) throws Exception
    {
    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    config.set("genome", genomeName);
    config.set("chromosome", chr);

    genomeAdmin.getGenome(genomeName).addChromosome(chr, 0, 0);

    Job job = new Job(config, "Reference Genome Fragmentation");

    job.setJarByClass(LoadFromFASTA.class);
    job.setMapperClass(FASTAFragmentMapper.class);

    job.setMapOutputKeyClass(LongWritable.class);
    //job.setMapOutputValueClass(ImmutableBytesWritable.class);
    job.setMapOutputValueClass(FragmentWritable.class);

    job.setInputFormatClass(FASTAInputFormat.class);
    FileInputFormat.addInputPath(job, path);

    // because we aren't emitting anything from mapper
    job.setOutputFormatClass(NullOutputFormat.class);

    //job.submit();
    return (job.waitForCompletion(true) ? 0 : 1);
    //return 0;
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

    if (fastaDir.startsWith("s3"))
      {
      Pattern s3pattern = Pattern.compile("^s3n?:\\/\\/(\\w+[-\\w+]*).*");
      Matcher s3file = s3pattern.matcher(fastaDir);
      if (!s3file.matches())
        throw new Exception("A S3 bucket name could not be determined from " + fastaDir);

      String bucket = s3file.group(1);
      Map<String, S3ObjectSummary> chromosomes = AWSUtils.listFASTAFiles(bucket);
      for (String chr : chromosomes.keySet())
        runTool(genome, chr, new LoadFromFASTA(genomeName, chr, AWSUtils.getS3Object(chromosomes.get(chr))));
      }
    else if (fastaDir.startsWith("hdfs"))
      {
      fastaDir = conf.get("fs.default.name") + "/" + fastaDir.replace("hdfs://", "");
      FileSystem fs = FileSystem.get(conf);
      log.info(fs.getWorkingDirectory().getName());
      FileStatus[] statuses = fs.listStatus(new Path(fastaDir), new PathFilter()
      {
      @Override
      public boolean accept(Path path)
        {
        return FileUtils.FASTA_FILE.accept(null, path.getName());
        }
      });
      for (FileStatus status: statuses)
        {
        String chr = FileUtils.getChromosomeFromFASTA(status.getPath().getName());
        runTool(genome, chr, new LoadFromFASTA(genomeName, chr, status.getPath()));
        }
      }
    else // local files
      {
      Map<String, File> files = org.lcsb.lu.igcsa.utils.FileUtils.getFASTAFiles(new File(fastaDir));
      for (String chr : files.keySet())
        runTool(genome, chr, new LoadFromFASTA(genomeName, chr, files.get(chr)));
      }
    }

  private static void runTool(HBaseGenome genome, String chr, LoadFromFASTA lff) throws Exception
    {
    if (genome.getChromosome(chr) == null)
      {
      final long startTime = System.currentTimeMillis();
      ToolRunner.run(lff, null);
      final long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("Finished job " + elapsedTime / 1000 + " seconds");
      }
    else
      log.info("Chromosome " + chr + " already exists. Skipping job.");
    }

  }




