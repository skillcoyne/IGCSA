package org.lcsb.lu.igcsa;

import org.apache.commons.cli.*;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class IGCSACommandLineParser extends BasicParser
  {
  private static IGCSACommandLineParser clp;

  private Options options;
  private Set<Option> requiredOpts;

  public static IGCSACommandLineParser getParser()
    {
    if (clp == null)
      clp = new IGCSACommandLineParser();
    return clp;
    }

  private IGCSACommandLineParser()
    {
    options = new Options();
    requiredOpts = new HashSet<Option>();
    }

  public void addOptions(Option... opts)
    {
    for (Option o: opts)
      {
      if (o.isRequired())
        {
        requiredOpts.add(o);
        o.setRequired(false);
        }
      options.addOption(o);
      }
    }

  public CommandLine parseOptions(String[] args) throws ParseException
    {
    CommandLine cl  = clp.parse(options, args);

    HelpFormatter help = new HelpFormatter();
    for (Option opt: requiredOpts)
      {
      if (!cl.hasOption(opt.getOpt()))
        {
        help.printHelp("Missing required option: -" + opt.getOpt() + " " + opt.getDescription(), options);
        System.exit(-1);
        }
      }

    return cl;
    }

  public Options getOptions()
    {
    return options;
    }
  }
