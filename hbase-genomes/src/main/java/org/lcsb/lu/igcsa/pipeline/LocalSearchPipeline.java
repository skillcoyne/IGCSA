/**
 * org.lcsb.lu.igcsa.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.job.BWAAlign;
import org.lcsb.lu.igcsa.job.MiniChromosomeJob;

import java.util.Iterator;


public class LocalSearchPipeline
  {
  private static final Log log = LogFactory.getLog(LocalSearchPipeline.class);

  public static CommandLine parseCommandLine(String[] args) throws ParseException
    {
    Options options = new Options();
    options.addOption(new Option("l", "location", true, "chromosome location ex. 5:29199-394421"));
    options.addOption(new Option("b", "bwa", true, "bwa archive location"));
    options.addOption(new Option("o", "output", true, "output path"));
    options.addOption(new Option("g", "genome", true, "parent genome name for sequence generation"));
    options.addOption(new Option("r", "reads", true, "read path for tsv"));


    CommandLine cl = new BasicParser().parse(options, args);

    HelpFormatter help = new HelpFormatter();
    if (!cl.hasOption("l") | !cl.hasOption("b") | !cl.hasOption("o") | !cl.hasOption("g") | !cl.hasOption("r"))
      {
      help.printHelp(LocalSearchPipeline.class.getSimpleName() + ":\nMissing required option. ", options);
      System.exit(-1);
      }

    return cl;
    }

  public static void main(String[] args) throws Exception
    {
    CommandLine cl = parseCommandLine(args);

    String indexPath = generateMiniAbrs(cl);
    System.out.println(indexPath);

    String alignedReads = alignReads(cl, indexPath);
    System.out.println(alignedReads);

    // score with streaming job

    }

  private static String generateMiniAbrs(CommandLine cl) throws Exception
    {
    log.info("*********** MINI CHR JOB *************");
    MiniChromosomeJob mcj = new MiniChromosomeJob();
    ToolRunner.run(mcj, new String[]{"-b", cl.getOptionValue("b"), "-g", cl.getOptionValue("g"), "-n", "mini", "-o", cl.getOptionValue("o"), "-l", cl.getOptionValue("l")});
    return mcj.getIndexPath().toString();
    }

  private static String alignReads(CommandLine cl, String indexPath) throws Exception
    {
    log.info("*********** ALIGN CHR JOB *************");
    String output = indexPath.substring(0, indexPath.indexOf("/index"));
    BWAAlign ba = new BWAAlign();
    ToolRunner.run(ba, new String[]{"--bwa-path", cl.getOptionValue("b"), "-n", "mini", "-i", indexPath, "-r", cl.getOptionValue("r"), "-o", output});
    ba.mergeSAM();
    return ba.getOutputPath().toString();
    }


  }
