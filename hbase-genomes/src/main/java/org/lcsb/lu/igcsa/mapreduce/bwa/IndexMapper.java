package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class IndexMapper extends Mapper<LongWritable, Text, Text, Text>
  {
  static Logger log = Logger.getLogger(IndexMapper.class.getName());
  //private static final String indexArchive = "index.tgz";
  private String bwa;

  public static void setIndexArchive(String name, Job job)
    {
    job.getConfiguration().set(job.getJobID() + ".bwa.index", name);
    }

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    bwa = context.getConfiguration().get("bwa.binary.path", "tools/bwa");

    log.info("**** BWA: "  + bwa);

//    Path bwaBinary = new Path(bwa);
//    FileSystem fs = FileSystem.get(bwaBinary.toUri(), context.getConfiguration());
//    if (!fs.exists(bwaBinary))
//      throw new RuntimeException("bwa binary does not exist in the cache at " + bwa);
    }

  @Override
  protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
    final Path refSrc = new Path(value.toString());

    String indexArchive =  refSrc.getName().substring(0, refSrc.getName().indexOf(".")) + ".tgz";

    FileSystem fs = FileSystem.get(refSrc.toUri(), context.getConfiguration());

    log.info(refSrc.toString());
    //if (!fs.exists(refSrc)) throw new IOException("Reference fasta file does not exist or is not readable: " + refSrc.toString());

    //String indexArchive = context.getConfiguration().get(context.getJobID() + ".bwa.index", "index.tgz");

//    if (fs.exists(new Path(refSrc, indexArchive)))
//      log.warn("BWA index already exists at " + refSrc.getParent() + " skipping indexing step.");
//    else
      {
      /**
       * TODO This doesn't work on s3.  Need to try to use the filesystem object instead
       */
      File tmpRefDir = new File(context.getTaskAttemptID() + "-" + key, "ref");
      tmpRefDir.mkdirs();

      File tmpFile = new File(tmpRefDir, refSrc.getName());
      FileUtil.copy(fs, refSrc, tmpFile, false, context.getConfiguration());

      log.info(tmpFile.exists());

      String indexCmd = String.format("%s index %s %s", bwa, "-a bwtsw", tmpFile.getPath());

      log.info("BWA cmd: " + indexCmd);

      ByteArrayOutputStream errorOS = new ByteArrayOutputStream();
      ByteArrayOutputStream outputOS = new ByteArrayOutputStream();

      // TODO need to report status periodically to keep hadoop from killing to job.

      int exitVal = new CommandExecution(context, errorOS, outputOS).execute(indexCmd);

      log.info(errorOS.toString());
      log.info(outputOS.toString());

      if (exitVal > 0) throw new RuntimeException("BWA error: " + errorOS.toString());

      File tmpZip = new File(tmpRefDir.getParentFile(), indexArchive);
      org.lcsb.lu.igcsa.utils.FileUtils.compressFiles(tmpRefDir.listFiles(), tmpZip.getAbsolutePath(), "ref");

      log.info(tmpZip.exists());

      if (tmpZip.exists())
        {
        Path dest = new Path(new Path(refSrc.getParent(), "index"), indexArchive);
        FileUtil.copy(tmpZip, fs, dest, false, context.getConfiguration());
//        fs.delete(refSrc, false);
        }
      else throw new IOException("Failed to create index archive for " + refSrc.toString());
      }
    }
  }
