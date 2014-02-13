package org.lcsb.lu.igcsa;

import org.apache.commons.cli.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.mapreduce.bwa.FastqToTSV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Align
  {
  static Logger log = Logger.getLogger(Align.class.getName());

  public static void main(String[] args) throws Exception
    {
    new Align(args);
    }

  public Align(String[] args) throws IOException
    {
    CommandLine cl = parseCommandLine(args);

    File archive = createArchive(cl.getOptionValue("b"));
    File tsv = createTSV(cl.getOptionValue("r"));

    // upload to hbase
    }


  // create zip file with BWA for upload
  private File createArchive(String localBWAPath) throws IOException
    {
    File bwa = new File(localBWAPath);
    if (!bwa.exists() || !bwa.canExecute())
      throw new IOException(bwa.getAbsolutePath() + " does not exist or is not an executable.");

    File archive = new File("/tmp/bwa.tgz");
    org.lcsb.lu.igcsa.utils.FileUtils.compressFiles(new File[]{bwa}, archive.getAbsolutePath(), "");

    if (!archive.exists())
      throw new IOException("Failed to create bwa archive at " + archive.getAbsolutePath());

    return archive;
    }

  // create read file tsv for upload
  private File createTSV(String readPairPath) throws IOException
    {
    File dir = new File(readPairPath);
    if (!dir.isDirectory() || !dir.canRead())
      throw new IOException(readPairPath + " is not a directory or is unreadable.");

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

    File tsv = new File("/tmp",  dir.getName() + ".tsv");
    new FastqToTSV(fastqFiles[0], fastqFiles[1]).toTSV( new FileOutputStream(tsv) );

    if (!tsv.exists())
      throw new IOException("Failed to create read TSV file at " + tsv.getAbsolutePath());

    return tsv;
    }


  private CommandLine parseCommandLine(String[] args)
    {
    Options options = new Options();

    options.addOption("b", "bwa-path", true, "Path to local installation of BWA");
    options.addOption("r", "read-dir", true, "Path to read pair directory with pair of FASTQ files");

    CommandLineParser clp = new BasicParser();
    CommandLine cl = null;

    try
      {
      cl = clp.parse(options, args);
      HelpFormatter help = new HelpFormatter();
      if (cl.hasOption('h') || !cl.hasOption('b') || !cl.hasOption('r'))
        {
        help.printHelp("<jar file>", options);
        System.exit(0);
        }
      }
    catch (ParseException e)
      {
      e.printStackTrace();
      }
    return cl;
    }
  }
