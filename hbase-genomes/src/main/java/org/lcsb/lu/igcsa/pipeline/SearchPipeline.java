/**
 * org.lcsb.lu.igcsa.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.cli.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.job.BWAAlign;
import org.lcsb.lu.igcsa.job.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.job.MiniChromosomeJob;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;

import java.io.IOException;


public abstract class SearchPipeline
  {
  private static final Log log = LogFactory.getLog(SearchPipeline.class);

  protected GenericOptionsParser gop;

  protected Options options;
  protected CommandLine commandLine;
  private IGCSACommandLineParser parser;

  protected SearchPipeline()
    {
    this.setOptions();
    parser = new IGCSACommandLineParser(true);

    }

  public Options getOptions()
    {
    return options;
    }

  public Configuration getConfiguration()
    {
    return gop.getConfiguration();
    }

  private void getHadoopOpts(String[] args)
    {
    gop = null;
    try
      {
      gop = new GenericOptionsParser(new Configuration(), args);
      }
    catch (IOException e)
      {
      log.error(e);
      }
    }

  protected abstract void usage();

  public CommandLine parseCommandLine(String[] args) throws ParseException
    {
    getHadoopOpts(args);
    commandLine = parser.parse(this.getOptions(), gop.getRemainingArgs());
    usage();

    return commandLine;
    }

  protected MiniChromosomeJob generateMiniAbrs(String[] generateArgs) throws Exception
    {
    log.info("*********** MINI CHR JOB *************");
    MiniChromosomeJob mcj = new MiniChromosomeJob(getConfiguration());
    ToolRunner.run(mcj, generateArgs);
    return mcj;
    }

  protected String alignReads(String indexPath, String name) throws Exception
    {
    log.info("*********** ALIGN CHR JOB *************");
    String output = new Path(indexPath.substring(0, indexPath.indexOf("/index")), "aligned").toString();
    BWAAlign ba = new BWAAlign(gop.getConfiguration());
    ToolRunner.run(ba, new String[]{"--bwa-path", commandLine.getOptionValue("b"), "-n", name, "-i", indexPath, "-r", commandLine.getOptionValue("r"), "-o", output});
    ba.mergeSAM();
    return ba.getOutputPath().toString();
    }

  /* To override */
  public abstract void setOptions();
  public abstract void runSearch(String[] args) throws Exception;


  }
