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
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.job.MiniChromosomeJob;

import java.util.ArrayList;
import java.util.List;



public class LocalSearchPipeline extends SearchPipeline
  {
  private static final Log log = LogFactory.getLog(LocalSearchPipeline.class);

  @Override
  public void setOptions()
    {
    options = new Options();
    Option opt = new Option("l", "location", true, "chromosome location ex. 5:29199-394421");
    opt.setRequired(true);
    options.addOption(opt);
    options.addOption(new Option("b", "bwa", true, "bwa archive location"));
    options.addOption(new Option("o", "output", true, "output path"));
    options.addOption(new Option("g", "genome", true, "parent genome name for sequence generation"));
    options.addOption(new Option("r", "reads", true, "read path for tsv"));
    }

  @Override
  protected void usage()
    {
    HelpFormatter help = new HelpFormatter();
    if (!commandLine.hasOption("l") | !commandLine.hasOption("b") | !commandLine.hasOption("o") | !commandLine.hasOption("g") | !commandLine.hasOption("r"))
      {
      help.printHelp(this.getClass().getSimpleName() + ":\nMissing required option. ", this.getOptions());
      System.exit(-1);
      }

    }

  public static void main(String[] args) throws Exception
    {
    new LocalSearchPipeline().runSearch(args);
    }


  @Override
  public void runSearch(String[] args) throws Exception
    {
    CommandLine cl = parseCommandLine(args);

    List<String> locs = new ArrayList<String>();
    for (String loc: cl.getOptionValues("l"))
      {
      locs.add("-l");
      locs.add(loc);
      }

    MiniChromosomeJob mcj = generateMiniAbrs((String[]) ArrayUtils.addAll(new String[]{"-b", cl.getOptionValue("b"), "-g", cl.getOptionValue("g"), "-n", "mini", "-o", cl.getOptionValue("o")}, locs.toArray(new String[locs.size()])));
    System.out.println(mcj.getIndexPath().toString());

    String alignedReads = alignReads(mcj.getIndexPath().toString(), mcj.getName());
    System.out.println(alignedReads);
    }
  }
