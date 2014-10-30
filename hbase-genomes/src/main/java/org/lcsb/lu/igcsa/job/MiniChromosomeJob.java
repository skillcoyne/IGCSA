/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.lang.ArrayUtils;
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
  private Path indexPath;
  private String name;

  public static void main(String[] args) throws Exception
    {
    MiniChromosomeJob mcj = new MiniChromosomeJob();
    ToolRunner.run(mcj, args);
    //System.out.println(mcj.getIndexPath().toString());
    }

  public MiniChromosomeJob()
    {
    super(new Configuration());
    setOpts();
    }

  public MiniChromosomeJob(Configuration conf)
    {
    super(conf);
    setOpts();
    }

  private void setOpts()
    {
    Option m = new Option("l", "location", true, "Locations, at least two are necessary.  Ex: -l 1:32-10000  -l 4:3990-50298");
    m.setRequired(false);
    this.addOptions(m);

    m = new Option("d", "band", true, "Bands, at least two are required.  Ex: -band 1q32 -band 5p11");
    m.setRequired(false);
    this.addOptions(m);

    m = new Option("o", "output", true, "output directory");
    m.setRequired(true);
    this.addOptions(m);

    m = new Option("g", "genome", true, "Parent genome");
    m.setRequired(true);
    this.addOptions(m);

    m = new Option("n", "name", true, "Directory name for mini chromosomes.");
    m.setRequired(true);
    this.addOptions(m);

    Option bwa = new Option("b", "bwa", true, "Path to bwa.tgz, optional.");
    bwa.setRequired(false);
    this.addOptions(bwa);

    DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:classpath:karyotype_probabilities", "igcsa", "");
    dao = conn.getKaryotypeDAO();
    }

  private void usage()
    {
    HelpFormatter hf = new HelpFormatter();
    hf.printHelp(this.getClass().getSimpleName() + " -l OR -d", this.options);
    System.exit(-1);
    }

  public Path getIndexPath()
    {
    return indexPath;
    }

  public String getName()
    {
    return name;
    }

  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parseOptions(args, this.getClass());
    //CommandLine cl = this.parser.parse(this.options, gop.getRemainingArgs(), false);
    //CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs(), this.options, this.getClass());
    //CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs(), this.getClass());

    log.info("ARGS: " + Arrays.toString(args));

    if ((cl.hasOption("location") && cl.hasOption("band")) || (!cl.hasOption("location") && !cl.hasOption("band"))) usage();

    getLocations(cl);

    String name = cl.getOptionValue("n");
    String parentGenomeName = cl.getOptionValue("g");
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

    this.name = derChrName;

    int i = 1;
    while (fs.exists(fastaOutput))
      {
      log.info(fastaOutput + " exists, trying " + i);
      fastaOutput = new Path(outputPath, derChrName + "_" + i);
      indexPath = new Path(fastaOutput, fastaOutput.getName() + ".fa");
      i++;
      }

    String desc = abr.getDescription() + ",bp=" + bands.get(0).getLocation().getLength();

    FASTAHeader header = new FASTAHeader(derChrName, name, "parent=" + parentGenomeName, desc);

    DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(new Configuration(), scan, fastaOutput, alf.getFilterLocationList(), abr, header);

    int ret = ToolRunner.run(gdc, null);
    try
      {
      indexPath = gdc.mergeOutputs(abr.getAberration(), fastaOutput, alf.getFilterLocationList().size());

      // Run BWA index
      if (cl.hasOption("b"))
        {
        log.info("Running index on " + indexPath.toString());
        BWAIndex bi = new BWAIndex(getConf());
        ret = ToolRunner.run(bi, (String[]) ArrayUtils.addAll(new String[]{"-b", cl.getOptionValue("b")}, new String[]{"-p", indexPath.toString()}));
        indexPath = bi.indexPath();
        }
      }
    catch (Exception e)
      {
      log.error(e);
      }

    return ret;
    }

  private void getLocations(CommandLine cl) throws Exception
    {
    locations = new ArrayList<Location>();
    bands = new ArrayList<Band>();

    if (cl.hasOption("l"))
      {
      if (cl.getOptionValues("l").length < 2) usage();

      for (String lop: cl.getOptionValues("l"))
        {
        String[] cloc = lop.split(":");
        String[] seloc = cloc[1].split("-");

        Location location = new Location(cloc[0], Integer.valueOf(seloc[0]), Integer.valueOf(seloc[1]));
        locations.add(location);

        for (Band b : dao.getBandDAO().getBands(location))
          {
          b.setLocation(location);
          bands.add(b);
          }
        }
      }
    else
      {
      if (cl.getOptionValues("band").length < 2) usage();
      for (String b: cl.getOptionValues("band"))
        {
        Band band = new Band(b);
        if (dao.getBandDAO().getBand(b) == null)
          throw new IllegalArgumentException("Band " + b + " does not exist.");

        band.setLocation(dao.getBandDAO().getLocation(band));
        bands.add(band);
        locations.add(band.getLocation());
        }
      }

    if (bands.size() != locations.size())
      throw new Exception("I have the wrong number of bands!");

    }

  }
