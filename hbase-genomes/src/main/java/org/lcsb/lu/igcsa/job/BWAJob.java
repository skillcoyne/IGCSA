package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class BWAJob extends JobIGCSA
  {
  static Logger log = Logger.getLogger(BWAJob.class.getName());

  public BWAJob(Configuration conf)
    {
    super(conf);

    Option bwa = new Option("b", "bwa-path", true, "Path to archive with bwa.");
    bwa.setRequired(true);

    this.addOptions(bwa);
    }

  protected void setupBWA(String bwaPath) throws URISyntaxException, IOException
    {
   /* WARNING: DistributedCache is not accessible on local runner (IDE) mode.
   In order to test in an IDE set the following config options using locally accessible
   files.
   -D bwa.binary.path=/usr/local/bin/bwa
   -D reference.fasta.path=/tmp/test6/ref/reference.fa
    */
    URI uri = new URI(new Path(bwaPath).toUri().toASCIIString() + "#tools");
    log.info("URI: " + uri.toASCIIString());

    if (bwaPath.endsWith(".tgz"))
      addArchive(uri);
    else
      throw new IOException("Expected a tgz archive: " + bwaPath);
    }




  }
