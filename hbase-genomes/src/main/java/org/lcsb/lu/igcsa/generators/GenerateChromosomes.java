/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generators;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.lcsb.lu.igcsa.DerivativeChromosomeJob;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.karyotype.database.util.DerbyConnection;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;

import java.util.ArrayList;
import java.util.List;


public class GenerateChromosomes
  {
  private static final Log log = LogFactory.getLog(GenerateChromosomes.class);
  private static KaryotypeDAO dao;

  public static void main(String[] args) throws Exception
    {
    IGCSACommandLineParser parser = IGCSACommandLineParser.getParser();
    parser.addOptions(new Option("o", "output", true, "output directory"),
                      new Option("n", "parent", true, "parent genome name"),
                      new Option("b", "bands", true, "comma separated band list"),
                      new Option("a", "all", false, "Include all locations before and after the provided bands. Default is false."));

    CommandLine cl = parser.parseOptions(args);
    if (!cl.hasOption("o") || !cl.hasOption("n") || !cl.hasOption("b"))
      {
      HelpFormatter help = new HelpFormatter();
      help.printHelp( cl.getArgs() + " found.  Missing one or more options: ", parser.getOptions());
      System.exit(-1);
      }
    String parentGenomeName = cl.getOptionValue("n");
    String outputDir = cl.getOptionValue("o");

    DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:classpath:karyotype_probabilities", "igcsa", "");
    dao = conn.getKaryotypeDAO();

    List<Band> bands = new ArrayList<Band>();
    for (String s: cl.getOptionValue("b").split(","))
      bands.add(new Band(s));

    for (Band b: bands)
      b.setLocation(dao.getBandDAO().getLocation(b));
    Aberration abr = new Aberration(bands, AberrationTypes.TRANSLOCATION);

    FASTAHeader header = new FASTAHeader(parentGenomeName + "-der", abr.getFASTAName(), "parent=" + parentGenomeName , abr.getDescription());

    List<Location> locations = JobUtils.getBandLocations(bands);
    if (cl.hasOption("a"))
      locations = JobUtils.getAllLocations(bands, dao);

    Scan scan = JobUtils.getScanFor(locations, parentGenomeName);

    Path baseOutput = new Path(outputDir, abr.getFASTAName());
    DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(HBaseConfiguration.create(), scan, baseOutput, locations, abr, header);
    gdc.run(null);
    gdc.mergeOutputs(abr.getAberration(), baseOutput, locations.size());
    }


  }
