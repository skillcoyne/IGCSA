package org.lcsb.lu.igcsa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;


/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class JobIGCSA extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(JobIGCSA.class.getName());

  public enum Paths
    {
      GENOMES("/genomes"),
      ALIGN("/bwaalignment"),
      TMP("/tmp");

    private String p;

    private Paths(String p)
      {
      this.p = p;
      }

    public String getPath()
      {
      return p;
      }
    }

  public JobIGCSA(Configuration conf)
    {
    super(conf);
    }

  public FileSystem getJobFileSystem() throws IOException
    {
    return FileSystem.get(getConf());
    }

  protected Path getPath(Paths p)
    {
    return new Path(p.getPath());
    }

  protected void addArchive(URI uri)
    {
    DistributedCache.addCacheArchive(uri, getConf());
    DistributedCache.createSymlink(getConf());
    log.info("Added to cache" + uri.toString());
    }


  }
