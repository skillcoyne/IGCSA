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
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.karyotype.database.util.DerbyConnection;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MiniChromosomeJob extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(MiniChromosomeJob.class);
  private static KaryotypeDAO dao;

  private List<Location> locations;
  private List<Band> bands;

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new MiniChromosomeJob(), args);


    }


  public MiniChromosomeJob()
    {
    super(new Configuration());

    Option m = new Option("l", "locs", true, "Locations comma separated.  Ex: 1:32-10000,4:3990-50298");
    m.setRequired(false);
    this.addOptions(m);

    m = new Option("b", "bands", true, "Bands comma separated.  Ex: 1q32,5p11.");
    m.setRequired(false);
    this.addOptions(m);

    m = new Option("o", "output", true, "output directory");
    m.setRequired(true);
    this.addOptions(m);

    m = new Option("p", "parent", true, "Parent genome");
    m.setRequired(true);
    this.addOptions(m);

    m = new Option("n", "name", true, "Directory name for mini chromosomes.");
    m.setRequired(true);
    this.addOptions(m);

    DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:classpath:karyotype_probabilities", "igcsa", "");
    dao = conn.getKaryotypeDAO();
    }

  public MiniChromosomeJob(Configuration conf)
    {
    super(conf);
    }


  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs(), this.getClass());

    log.info("ARGS: " + Arrays.toString(args));

    if (args.length < 4 || (cl.hasOption("l") && cl.hasOption("b")))
      //if (args.length < 4)
      {
      HelpFormatter hf = new HelpFormatter();
      hf.printHelp("Usage: " + this.getClass().getSimpleName() + "-l OR -b", this.parser.getOptions());
      System.exit(-1);
      }
    getLocations(cl);

    String name = cl.getOptionValue("n");
    String parentGenomeName = cl.getOptionValue("p");
    Path outputDir = new Path(cl.getOptionValue("o"));

    Aberration abr = new Aberration(bands, AberrationTypes.TRANSLOCATION);

    AberrationLocationFilter alf = new AberrationLocationFilter();
    alf.getFilter(locations, parentGenomeName);

    Scan scan = new Scan();
    scan.setFilter(alf.getFilterList());

    String derChrName = bands.get(0).getFullName();
    if (bands.size() > 1)
      derChrName = bands.get(0).getFullName() + "-" + bands.get(1).getFullName();
    FileSystem fs = FileSystem.get(outputDir.toUri(), new Configuration());
    Path outputPath = new Path(outputDir, name);
    Path fastaOutput = new Path(outputPath, derChrName);
    if (fs.exists(fastaOutput))
      fs.delete(fastaOutput, true);

    FASTAHeader header = new FASTAHeader(derChrName, name, "parent=" + parentGenomeName, abr.getDescription());
    DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(new Configuration(), scan, fastaOutput, alf.getFilterLocationList(), abr, header);
    //Job job = gdc.createJob(null);

    int ret = ToolRunner.run(gdc, null);
    try
      {
      gdc.mergeOutputs(abr.getAberration(), fastaOutput, alf.getFilterLocationList().size());
      }
    catch (Exception e)
      {
      log.error(e);
      }


    BWAIndex.main(new String[]{"-p", fastaOutput.getParent().toString(), "-b", cl.getOptionValue("b")});



    return ret;

    //return (job.waitForCompletion(true) ? 0 : 1);
    }

  private void getLocations(CommandLine cl)
    {
    locations = new ArrayList<Location>();
    bands = new ArrayList<Band>();

    if (cl.hasOption("l"))
      {
      for (String lop : cl.getOptionValue("l").split(","))
        {
        String[] cloc = lop.split(":");
        String[] seloc = cloc[1].split("-");

        Location location = new Location(cloc[0], Integer.valueOf(seloc[0]), Integer.valueOf(seloc[1]));
        locations.add(location);

        for (Band b : dao.getBandDAO().getBands(location))
          bands.add(b);
        }
      }
    else
      {
      for (String b : cl.getOptionValue("b").split(","))
        {
        Band band = new Band(b);
        if (dao.getBandDAO().getBand(b) == null)
          throw new IllegalArgumentException("Band " + b + " does not exist.");

        band.setLocation(dao.getBandDAO().getLocation(band));
        bands.add(band);
        locations.add(band.getLocation());
        }
      }

    }

  }
