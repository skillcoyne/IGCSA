/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.lcsb.lu.igcsa.karyotype.database.util.DerbyConnection;


public class MiniChromosomeJob extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(MiniChromosomeJob.class);

  public static void main(String[] args) throws Exception
    {

    IGCSACommandLineParser parser = IGCSACommandLineParser.getParser();
    parser.addOptions(new Option("f", "file", true, "Karyotype file"), new Option("c", "cell-line", true, "cell line name"), new Option("o", "output", true, "output directory"), new Option("b", "bands", true, "generate specific bands"));

    CommandLine cl = parser.parseOptions(args);
    if (!cl.hasOption("o") || !cl.hasOption("c"))
      {
      HelpFormatter help = new HelpFormatter();
      help.printHelp("Missing output directory or cell line name.", parser.getOptions());
      System.exit(-1);
      }
    else if (!cl.hasOption("b") && !cl.hasOption("f") || (cl.hasOption("b") && cl.hasOption("f")))
      {
      HelpFormatter help = new HelpFormatter();
      help.printHelp("Missing bands or karyotype file or attempted to use both.", parser.getOptions());
      System.exit(-1);
      }


    DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:classpath:karyotype_probabilities", "igcsa", "");
    dao = conn.getKaryotypeDAO();

    }


  public MiniChromosomeJob(Configuration conf)
    {
    super(conf);
    }


  public int run(String[] strings) throws Exception
    {
    return 0;
    }
  }
