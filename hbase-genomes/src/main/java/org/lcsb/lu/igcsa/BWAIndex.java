/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.util.Tool;

import java.io.*;


public class BWAIndex extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(BWAIndex.class);

  private Configuration conf;
  private Path fasta;

  public static void main(String[] args) throws Exception
    {
    new BWAIndex().run(args);
    }

  // TODO this only runs if bwa is installed on your system - also a bit broken at the moment
  public int run(String[] args) throws Exception
    {
    this.fasta = new Path(args[0]);
    this.conf = new Configuration();

    log.info("FASTA: " + fasta.toString());

    FileSystem fs = fasta.getFileSystem(conf);
    File localFasta = new File(new File("/tmp/" + fasta.getParent().getName()), fasta.getName());

    // need a local directory to copy things to if it doesn't already exist
    if (fs.getUri().toASCIIString().equals("hdfs:///"))
      {
      if (localFasta.getParentFile().exists()) FileUtils.cleanDirectory(localFasta.getParentFile());
      else localFasta.getParentFile().mkdir();
      }

    log.info(fs.getFileStatus(fasta).getPath() + " " + fs.getFileStatus(fasta).getLen());

    //Set up dir for bwa
    FileUtil.copy(fs, fasta, localFasta, false, conf);
    log.info("Copy to local fasta: " + localFasta);

    String bwaCmd = "bwa index -a bwtsw " + localFasta.getAbsolutePath();
    Runtime rt = Runtime.getRuntime();
    StreamWrapper error, output;

    log.info(bwaCmd);
    Process p = Runtime.getRuntime().exec(bwaCmd);
    error = getStreamWrapper(p.getErrorStream(), "ERROR");
    output = getStreamWrapper(p.getInputStream(), "OUTPUT");
    int exitVal = 0;

    error.start();
    output.start();
    error.join(3000);
    output.join(3000);
    exitVal = p.waitFor();

    log.info("BWA Output: " + output.message);

    if (exitVal > 0) throw new Exception("Failed to run bwa: " + error.message);

    File[] indexFiles = localFasta.getParentFile().listFiles(new FilenameFilter()
    {
    public boolean accept(File file, String name)
      {
      return (name.startsWith(FilenameUtils.getBaseName(fasta.getName())));
      }
    });

    File zip = new File(localFasta.getParentFile(), "index.tgz");
    org.lcsb.lu.igcsa.utils.FileUtils.compressFiles(indexFiles, zip.getAbsolutePath(), "ref");

    FileUtil.copy(zip, fs, new Path(fasta.getParent(), zip.getName()), true, conf);
    fs.deleteOnExit(this.fasta); // this file is in the tgz, no reason to keep it duplicated

    FileUtils.deleteDirectory(localFasta.getParentFile()); // clean up local directory

    return 0;
    }

  public static StreamWrapper getStreamWrapper(InputStream is, String type)
    {
    return new StreamWrapper(is, type);
    }

  public static class StreamWrapper extends Thread
    {
    InputStream is = null;
    String type = null;
    String message = null;

    public String getMessage()
      {
      return message;
      }

    StreamWrapper(InputStream is, String type)
      {
      this.is = is;
      this.type = type;
      }

    public void run()
      {
      try
        {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = null;
        while ((line = br.readLine()) != null)
          {
          buffer.append(line);
          buffer.append("\n");
          }
        message = buffer.toString();
        }
      catch (IOException ioe)
        {
        ioe.printStackTrace();
        }
      }
    }

  /*
  This doesn't work.  BWA isn't able to read the file from hdfs even when run within a mapper.
   */
//  public int run(String[] args) throws Exception
//    {
//    //args = new String[]{"/tmp/GRCh37-kiss1/merged.txt"};
//    String input = args[0];
//
//    log.info(input);
//
//    Path inputPath = new Path(input);
//
//    Configuration conf = new Configuration();
//
//    // TODO this is a temporary measure
//    URI uri = new URI("hdfs://localhost:9000/bwa-tools/bwa.tgz#tools");
//
//    // WARNING: DistributedCache is not accessible on local runner (IDE) mode.  Has to run as a hadoop job to test
//    DistributedCache.addCacheArchive(uri, conf);
//    DistributedCache.createSymlink(conf);
//
//    Job job = new Job(conf, "BWA Index");
//    job.setJarByClass(Test.class);
//
//    job.setMapperClass(IndexMapper.class);
//    job.setInputFormatClass(KeyValueTextInputFormat.class);
//    //job.setInputFormatClass(WholeFileInputFormat.class);
//    //    job.setMapOutputKeyClass(Text.class);
//    //    job.setMapOutputValueClass(Text.class);
//    FileInputFormat.addInputPath(job, inputPath);
//
//    //job.setReducerClass(IndexReducer.class);
//    job.setNumReduceTasks(0);
//    job.setOutputFormatClass(NullOutputFormat.class);
//
//    //FileOutputFormat.setOutputPath(job, inputPath.getParent());
//
//    return (job.waitForCompletion(true) ? 0 : 1);
//
//    }
//
//
//
//  static class IndexMapper extends Mapper<Text, Text, Text, Text>
//    {
//    private String bwaBinary = "tools/bwa";
//
//    @Override
//    protected void setup(Context context) throws IOException, InterruptedException
//      {
//      if (!new File(bwaBinary).exists()) throw new RuntimeException("bwa binary does not exist in the cache.");
//
//      log.info("BWA BINARY FOUND");
//
//      super.setup(context);
//      }
//
//
//    @Override
//    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException
//      {
//
//      String cmd = bwaBinary + " index -a bwtsw " + value.toString();
//      log.info("BWA cmd: " + cmd);
//
//      Process p = Runtime.getRuntime().exec(cmd);
//
//      BWAIndex.StreamWrapper error, output;
//      error = BWAIndex.getStreamWrapper(p.getErrorStream(), "ERROR");
//      output = BWAIndex.getStreamWrapper(p.getInputStream(), "OUTPUT");
//      int exitVal = 0;
//
//      error.start();
//      output.start();
//      error.join(3000);
//      output.join(3000);
//      exitVal = p.waitFor();
//
//      log.info("-- " + exitVal + " --");
//      log.info("BWA OUTPUT: " + output.message);
//      log.info("BWA ERROR: " + error.message);
//
//      if (exitVal > 0) throw new RuntimeException("BWA error: " + error.message);
//
//      //super.map(key, value, context);
//      }
//    }


  }

