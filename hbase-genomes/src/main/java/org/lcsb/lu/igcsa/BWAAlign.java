package org.lcsb.lu.igcsa;

import com.m6d.filecrush.crush.Crush;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.mapreduce.NullMapper;
import org.lcsb.lu.igcsa.mapreduce.NullReducer;
import org.lcsb.lu.igcsa.mapreduce.bwa.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class BWAAlign extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(BWAAlign.class.getName());

  private File[] fastqPair;
  private Configuration conf;
  private String readPairName;
  private Path refGenome;
  private Path outputPath;

  private static Path alignOutput = new Path("/bwaalignment");


  public BWAAlign(File[] fastqFiles, String genome) throws Exception
    {
    fastqPair = fastqFiles;
    readPairName = fastqPair[0].getParentFile().getName();
    refGenome = new Path(genome);
    }

  public Path getOutputPath()
    {
    return outputPath;
    }

  public FileSystem getFileSystem() throws IOException
    {
    return FileSystem.get(conf);
    }

  private void setup() throws URISyntaxException, IOException
    {
    conf = new Configuration();
    conf.setBoolean(SAMOutputFormat.HEADER_OUTPUT, false);
    conf.set("fs.default.name","hdfs://localhost:9000");

    // WARNING: DistributedCache is not accessible on local runner (IDE) mode.  Has to run as a hadoop job to test
    URI uri = new URI("hdfs://localhost:9000/bwa-tools/bwa.tgz#tools");
    DistributedCache.addCacheArchive(uri, conf);
    log.info("Added " + uri.toString());
    DistributedCache.createSymlink(conf);

    FileSystem fs = FileSystem.get(conf);
    if (!fs.exists(alignOutput))
      fs.mkdirs(alignOutput);

    Path reference = new Path(refGenome, "index.tgz");
    if (!fs.exists(reference))
      throw new IOException("Indexed reference genome does not exist: " + reference.toUri());
    reference = reference.makeQualified(fs);

    outputPath = new Path(new Path(alignOutput, readPairName), refGenome.getName());
    if (fs.exists(outputPath))
      fs.delete(outputPath, true);

    // reference
    uri = new URI(reference.toUri().toASCIIString() + "#reference");
    DistributedCache.addCacheArchive(uri, conf);
    log.info("Added " + uri.toString());

    DistributedCache.createSymlink(conf);
    }

  @Override
  public int run(String[] args) throws Exception
    {
    setup();

    Path readsOutput = new Path(alignOutput, "reads");
    // TODO: this is fine on a local system but will not work for ec2
    Path tsvInput = new FastqToTSV(fastqPair[0], fastqPair[1]).toTSV(readsOutput.getFileSystem(conf), readsOutput);

    // set up the job
    Job job = new Job(conf, "Align read pairs.");

    job.setJarByClass(BWAAlign.class);
    job.setMapperClass(ReadPairMapper.class);

    job.setInputFormatClass(ReadPairTSVInputFormat.class);
    TextInputFormat.addInputPath(job, tsvInput);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);

    job.setReducerClass(SAMReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setOutputFormatClass(SAMOutputFormat.class);

    FileOutputFormat.setOutputPath(job, outputPath);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static void main(String[] args) throws Exception
    {
    args = new String[]{"/Users/sarah.killcoyne/Data/1000Genomes/Reads/ERX000272/unzipped", "/tmp/test6"};
    if (args.length < 2)
      {
      System.err.println("Missing required arguments. Usage <read-pair directory> <ref genome path>");
      System.exit(-1);
      }

    String readPairDir = args[0];
    File dir = new File(readPairDir);
    if (!dir.isDirectory() || !dir.canRead())
      throw new IOException(readPairDir + " is not a directory or is unreadable.");

    File[] fastqFiles = dir.listFiles(new FilenameFilter()
    {
    @Override
    public boolean accept(File file, String name)
      {
      return (name.endsWith(".fastq") || name.endsWith(".fastq.gz"));
      }
    });

    if (fastqFiles.length != 2)
      throw new IOException(dir + " contains " + fastqFiles.length + " fastq files. Please ensure directory contains a single set of read-pair files.");

    BWAAlign align = new BWAAlign(fastqFiles, args[1]);
    ToolRunner.run(align, null);

    for (FileStatus status : align.getFileSystem().listStatus(align.getOutputPath()) )
      {
      if (status.getLen() <= 0)
        align.getFileSystem().delete(status.getPath(), true);
      }

    // Merge the files into a single SAM
    ToolRunner.run(new Crush(), new String[]{"--input-format=text", "--output-format=text", "--compress=none",
        align.getOutputPath().toString(),
        align.getOutputPath().toString() + ".sam" });
    // drop the unmerged data
    align.getFileSystem().deleteOnExit(align.getOutputPath());


    }


  }
