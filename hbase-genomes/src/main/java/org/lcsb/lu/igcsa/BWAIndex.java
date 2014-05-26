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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.mapreduce.bwa.IndexMapper;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAFragmentInputFormat;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil;
import org.lcsb.lu.igcsa.utils.FileUtils;

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

  private void setupRefs(Path path) throws IOException
    {
    final FileSystem fs = FileSystem.get(path.toUri(), getConf());



    FileStatus[] referencePaths = fs.listStatus(path, new PathFilter()
    {
    public boolean accept(Path p)
      {
      return p.toString().matches("^.*\\.(fa|fasta)$");
      //dir = fs.getFileStatus(p).isDir();
      }
    });

    if (referencePaths.length <= 0)
      throw new IOException("No paths found under parent path: " + path.toString());


    fastaPath = new Path(path.getParent(), "refs_to_index.txt");
    FSDataOutputStream os = fs.create(fastaPath);
    for (FileStatus status : referencePaths)
      os.write( (status.getPath().toUri().getPath().toString() + "\n").getBytes());
      //os.writeChars(status.getPath().toUri().getPath().toString() + "\n"); // strips out the hdfs:// or file://
    os.close();
    }

  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());

    setupRefs(new Path(cl.getOptionValue('p')));

    log.info("Running BWAIndex on " + fastaPath.getParent().toString());

    setupBWA(cl.getOptionValue('b'));

    Job job = new Job(getConf(), "BWA Index for " + fastaPath.getParent().toString());

    job.setJarByClass(BWAIndex.class);

    // set the name for the archive, the "prefix" in bwa
    IndexMapper.setIndexArchive(FilenameUtils.getBaseName(fastaPath.getName()), job);

    job.setMapperClass(IndexMapper.class);
    job.setInputFormatClass(TextInputFormat.class);
    FileInputFormat.addInputPath(job, fastaPath);

    job.setNumReduceTasks(0);
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  }

