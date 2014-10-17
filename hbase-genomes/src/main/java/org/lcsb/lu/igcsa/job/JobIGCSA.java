package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;


/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class JobIGCSA extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(JobIGCSA.class.getName());

  protected Options options = new Options();
  protected IGCSACommandLineParser parser = IGCSACommandLineParser.getParser();


  public JobIGCSA(Configuration conf)
    {
    super(conf);
    }

  public FileSystem getJobFileSystem() throws IOException
    {
    return FileSystem.get(getConf());
    }

  public FileSystem getJobFileSystem(URI uri) throws IOException
    {
    return FileSystem.get(uri, getConf());
    }

  protected void addArchive(URI uri)
    {
    DistributedCache.addCacheArchive(uri, getConf());
    DistributedCache.createSymlink(getConf());
    log.info("Added to cache " + uri.toString());
    log.info("symlink: " + DistributedCache.getSymlink(getConf()));
    }

  protected void addFile(URI uri)
    {
    DistributedCache.addCacheFile(uri, getConf());
    DistributedCache.createSymlink(getConf());
    log.info("Added to cache " + uri.toString());
    log.info("symlink: " + DistributedCache.getSymlink(getConf()));
    }

  protected void addArchive(URI uri, boolean symlink)
    {
    log.info("Adding archive " + uri.toString());
    if (symlink)
      addArchive(uri);
    else
      DistributedCache.addCacheArchive(uri, getConf());
    }

  protected void addOptions(Option opt)
    {
     this.options.addOption(opt);
    //this.parser.addOptions(opts);
    }

  protected GenericOptionsParser parseHadoopOpts(String[] args) throws ParseException
    {
    GenericOptionsParser gop = null;
    try
      {
      gop = new GenericOptionsParser(getConf(), args);
      }
    catch (IOException e)
      {
      log.error(e);
      }
    return gop;
    }

  public CommandLine parseOptions(String[] args, Class currentClass) throws ParseException
    {
    //parser.setOptions(options);
    CommandLine cl = parser.parse(options, args, false);

    HelpFormatter help = new HelpFormatter();

    Iterator<Option> oI = options.getOptions().iterator();
    while(oI.hasNext())
      {
      Option opt = oI.next();
      if (opt.isRequired() && !cl.hasOption(opt.getOpt()))
        {
        help.printHelp(currentClass.getSimpleName() + ":\nMissing required option: -" + opt.getOpt() + " " + opt.getDescription(), options);
        System.exit(-1);
        }
      }
    return cl;
    }

  protected void checkPath(Path path, boolean overwrite) throws IOException
    {
    FileSystem fs = getJobFileSystem();
    if (path.toString().startsWith("s3")) fs = getJobFileSystem(path.toUri());

    if (fs.exists(path))
      {
      log.info("Overwriting output path " + path.toString());
      fs.delete(path, true);
      }
    }

  }
