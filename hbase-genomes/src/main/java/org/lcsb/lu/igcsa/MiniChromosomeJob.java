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
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.karyotype.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.karyotype.database.util.DerbyConnection;


public class MiniChromosomeJob extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(MiniChromosomeJob.class);

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new MiniChromosomeJob(), args);



    }


  public MiniChromosomeJob()
    {
    super(new Configuration());

    Option m = new Option("b", "bands", true, "Band pairs, comma separated.");
    m.setRequired(true);
    this.addOptions(m);

    m = new Option("o", "output", true, "output directory");
    m.setRequired(true);
    this.addOptions(m);

    m = new Option("p", "parent", true, "Parent genome");
    m.setRequired(true);
    this.addOptions(m);

    }

  public MiniChromosomeJob(Configuration conf)
    {
    super(conf);
    }


  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());
    if (args.length < 3)
      {
      System.err.println("Usage: " + this.getClass().getSimpleName() + " -p <parent genome> -b <band pairs, comma sep> -o <output path>");
      System.exit(-1);
      }


    DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:classpath:karyotype_probabilities", "igcsa", "");
    KaryotypeDAO dao = conn.getKaryotypeDAO();

    return 0;
    }
  }
