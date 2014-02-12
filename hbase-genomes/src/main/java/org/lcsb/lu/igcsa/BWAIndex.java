/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.lcsb.lu.igcsa.mapreduce.bwa.CommandExecution;

import java.io.*;
import java.net.URI;


public class BWAIndex extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(BWAIndex.class);

  private Configuration conf;
  private Path fasta;

  public static void main(String[] args) throws Exception
    {
    if (args.length < 1)
      {
      System.err.println("Missing argument, <path to file>.");
      System.exit(-1);
      }

    new BWAIndex().run(args);
    }


  public static Path writeReferencePointerFile(Path mergedFasta, FileSystem fs) throws IOException
    {
    Path tmp = new Path(mergedFasta.getParent(), "ref.txt");

    FSDataOutputStream os = fs.create(tmp);
    os.write( mergedFasta.toString().getBytes() );
    os.write( "\n".getBytes() );
    os.flush(); os.close();

    return tmp;
    }

  public int run(String[] args) throws Exception
    {
    Path inputPath = new Path(args[0]);

    Configuration conf = new Configuration();
    //conf.set("fs.default.name", "hdfs://localhost:9000");

    // TODO this needs to be part of the setup
    URI uri = new URI("hdfs://localhost:9000/bwa-tools/bwa.tgz#tools");

    // WARNING: DistributedCache is not accessible on local runner (IDE) mode.  Has to run as a hadoop job to test
    DistributedCache.addCacheArchive(uri, conf);
    DistributedCache.createSymlink(conf);

    Job job = new Job(conf, "BWA Index");
    job.setJarByClass(BWAIndex.class);

    job.setMapperClass(IndexMapper.class);
    job.setInputFormatClass(TextInputFormat.class);
    FileInputFormat.addInputPath(job, inputPath);

    job.setNumReduceTasks(0);
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  static class IndexMapper extends Mapper<LongWritable, Text, Text, Text>
    {
    private String bwa;
    private static final String indexArchive = "index.tgz";

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      bwa = context.getConfiguration().get("bwa.binary.path", "tools/bwa");

      File bwaBinary = new File(bwa);
      if (!bwaBinary.exists())
        {
        bwa = "/usr/local/bin/bwa"; // to run in the IDE
        //throw new RuntimeException("bwa binary does not exist in the cache.");
        }
      log.info("BWA BINARY FOUND: " + bwaBinary);
      }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
      {
      FileSystem fs = FileSystem.get(context.getConfiguration());

      final Path refSrc = new Path(value.toString());
      if (!fs.exists(refSrc)) throw new IOException("Reference fasta file does not exist or is not readable: " + refSrc.toString());

      if (fs.exists(new Path(refSrc.getParent(), indexArchive)))
        log.warn("BWA index already exists at " + refSrc.getParent() + " skipping indexing step.");
      else
        {
        File tmpRefDir = new File(context.getTaskAttemptID() + "-" + key, "ref");
        tmpRefDir.mkdirs();

        File tmpFile = new File(tmpRefDir, refSrc.getName());
        FileUtil.copy(fs, refSrc, tmpFile, false, context.getConfiguration());

        log.info(tmpFile.exists());

        String indexCmd = String.format("%s index %s %s", bwa, "-a bwtsw", tmpFile.getPath());

        log.info("BWA cmd: " + indexCmd);

        ByteArrayOutputStream errorOS = new ByteArrayOutputStream();
        ByteArrayOutputStream outputOS = new ByteArrayOutputStream();

        int exitVal = new CommandExecution(context, errorOS, outputOS).execute(indexCmd);

        log.info(errorOS.toString());
        log.info(outputOS.toString());

        if (exitVal > 0) throw new RuntimeException("BWA error: " + errorOS.toString());

        File tmpZip = new File(tmpRefDir.getParentFile(), indexArchive);
        org.lcsb.lu.igcsa.utils.FileUtils.compressFiles(tmpRefDir.listFiles(), tmpZip.getAbsolutePath(), "ref");

        log.info(tmpZip.exists());

        if (tmpZip.exists())
          {
          FileUtil.copy(tmpZip, fs, refSrc.getParent(), false, context.getConfiguration());
          fs.delete(refSrc, false);
          }
        else throw new IOException("Failed to create index archive for " + refSrc.toString());
        }
      }
    }
  }

