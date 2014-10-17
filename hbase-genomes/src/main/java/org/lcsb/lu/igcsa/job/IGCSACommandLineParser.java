package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class IGCSACommandLineParser extends BasicParser
  {
  private static final Log log = LogFactory.getLog(IGCSACommandLineParser.class);

  private static IGCSACommandLineParser clp;

  private boolean ignoredUnrecognized;

  public IGCSACommandLineParser(boolean ignoredUnrecognized)
    {
    this.ignoredUnrecognized = ignoredUnrecognized;
    }

  @Override
  protected void processOption(String arg, ListIterator iter) throws ParseException
    {
    boolean hasOption = getOptions().hasOption(arg);

    if (hasOption || !ignoredUnrecognized)
      super.processOption(arg, iter);
    else
      log.warn("Ignoring option '" + arg + "'");
    }

  public static IGCSACommandLineParser getParser()
    {
    clp = new IGCSACommandLineParser(true);
    return clp;
    }

  }
