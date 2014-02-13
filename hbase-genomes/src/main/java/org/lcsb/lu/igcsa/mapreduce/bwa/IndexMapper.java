package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
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
  private static final String indexArchive = "index.tgz";
  private String bwa;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    bwa = context.getConfiguration().get("bwa.binary.path", "tools/bwa");

    File bwaBinary = new File(bwa);
    if (!bwaBinary.exists())
      throw new RuntimeException("bwa binary does not exist in the cache at " + bwa);
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
