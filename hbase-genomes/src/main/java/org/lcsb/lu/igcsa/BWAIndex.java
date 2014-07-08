/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.bwa.IndexMapper;

import java.io.*;


/*
Inputs:
-f reference genome text file (format, each line provides the path to a reference.fa file)
-b bwa tgz archive in hdfs
 */
public class BWAIndex extends BWAJob
  {
  private static final Log log = LogFactory.getLog(BWAIndex.class);

  private Path fastaPath;

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new BWAIndex(), args);
    }

  public static Path writeReferencePointerFile(Path mergedFasta, FileSystem fs) throws IOException
    {
    Path tmp = new Path(mergedFasta.getParent(), "ref.txt");

    FSDataOutputStream os = fs.create(tmp);
    os.write(mergedFasta.toString().getBytes());
    os.write("\n".getBytes());
    os.flush();
    os.close();

    return tmp;
    }

  public BWAIndex()
    {
    super(new Configuration());

    Option genome = new Option("p", "path", true, "Path to FASTA files");
    genome.setRequired(true);
    this.addOptions(genome);
    }

  private Path setupRefFile(Path path) throws IOException
    {
    Path fastaTxt = path;
    Path all = null;

    final FileSystem fs = FileSystem.get(path.toUri(), getConf());

    Path tmpPath = new Path("/tmp/" + System.currentTimeMillis() + "/all.fa");
    FileStatus[] fastaFiles = fs.globStatus(new Path(path, "*.{fa,fasta}"));
    if (fastaFiles.length > 1)
      {
      if (FileUtil.copyMerge(fs, fastaTxt, fs, tmpPath, false, getConf(), "\n"))
        {
        FileUtil.copy(fs, tmpPath, fs, new Path(fastaTxt, "all.fa"), false, false, getConf());
        fs.delete(tmpPath.getParent(), true);
        }
      all = new Path(fastaTxt, "all.fa");
      fastaTxt = BWAIndex.writeReferencePointerFile(all, fs);
      }
    else
      {
      all = new Path(fastaFiles[0].getPath().getParent(), "all.fa");
      FileUtil.copy(fs, fastaFiles[0].getPath(), fs, all , false, false, getConf());
      fastaTxt = BWAIndex.writeReferencePointerFile(all, fs);
      }
    fs.deleteOnExit(fastaTxt);
    fs.deleteOnExit(all);

    return fastaTxt;
    }

  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());

    Path fastaTxt = setupRefFile(new Path(cl.getOptionValue('p')));

    log.info("Running BWAIndex on " + fastaTxt.getParent().toString());

    setupBWA(cl.getOptionValue('b'));

    Job job = new Job(getConf(), "BWA Index for " + fastaTxt.getParent().toString());
    job.setJarByClass(BWAIndex.class);


    IndexMapper.setIndexArchive(FilenameUtils.getBaseName(fastaTxt.getName()), job);

    job.setMapperClass(IndexMapper.class);
    job.setInputFormatClass(TextInputFormat.class);
    FileInputFormat.addInputPath(job, fastaTxt);

    job.setNumReduceTasks(0);
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

                           //    FileStatus[] referencePaths = fs.listStatus(path, new PathFilter()
                           //    {
                           //    public boolean accept(Path p)
                           //      {
                           //      return p.toString().matches("^.*\\.(fa|fasta)$");
                           //      //dir = fs.getFileStatus(p).isDir();
                           //      }
                           //    });
                           //    if (referencePaths.length <= 0) throw new IOException("No paths found under parent path: " + path.toString());

  }

