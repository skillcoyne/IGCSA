/**
 * PACKAGE_NAME
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.DerivativeChromosomeJob;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.karyotype.database.util.DerbyConnection;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.util.*;


public class GenerateSpecialPatient
  {
  private static final Log log = LogFactory.getLog(GenerateSpecialPatient.class);
  private static KaryotypeDAO dao;

  private static String newGenomeName;
  private static String parentGenomeName;
  private static Path outputDir;
  private static List<Band> translocationBands;

  public static void generateBandPairs() throws ProbabilityException
    {
    Map<Object, Double> instability = dao.getGeneralKarytoypeDAO().getChromosomeInstability().getRawProbabilities();
    List<Object> keys = new ArrayList(instability.keySet());

    String lowChr = (String) keys.get(  new RandomRange(1,6).nextInt() );
    String highChr = (String) keys.get( new RandomRange(17,23).nextInt() );

    Probability low = dao.getGeneralKarytoypeDAO().getBandProbabilities(lowChr);
    Probability high = dao.getGeneralKarytoypeDAO().getBandProbabilities(highChr);

    // roll each until you don't get a centromere
    Band lowB = (Band) low.roll();
    while (lowB.isCentromere())
      lowB = (Band) low.roll();

    Band highB = (Band) high.roll();
    while (highB.isCentromere())
      highB = (Band) low.roll();

    log.info("Low band: " + lowB);
    log.info("High band: " + " " + highB);
    }


  private static CommandLine readCommandLine(String[] args) throws ParseException
    {
    IGCSACommandLineParser parser = IGCSACommandLineParser.getParser();
    parser.addOptions(new Option("g", "genome", true, "parent genome"),
        new Option("n", "name", true, "New genome name to write fasta files to."),
        new Option("o", "output", true, "output directory"),
        new Option("b", "bands", true, "Generate chromosome with a translocation at the given bands."));

    CommandLine cl = parser.parseOptions(args, GenerateSpecialPatient.class);
    if (!cl.hasOption("o") || !cl.hasOption("g") || !cl.hasOption("b") || !cl.hasOption("n"))
      {
      HelpFormatter help = new HelpFormatter();
      help.printHelp("Missing option, all are required.", parser.getOptions());
      System.exit(-1);
      }

    return cl;
    }

  public static void main(String[] args) throws Exception
    {
    CommandLine cl = readCommandLine(args);

    translocationBands = new ArrayList<Band>();
    for (String b : cl.getOptionValue("b").split(","))
      translocationBands.add(new Band(b));

    newGenomeName = cl.getOptionValue("n");
    parentGenomeName = cl.getOptionValue("g");
    outputDir = new Path(cl.getOptionValue("o"));

    DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:classpath:karyotype_probabilities", "igcsa", "");
    dao = conn.getKaryotypeDAO();

    new GenerateSpecialPatient().generate();
    //generateBandPairs();
    }

  public GenerateSpecialPatient()
    {
    }

  public void generate() throws Exception
    {
    Aberration abr = new Aberration(translocationBands, AberrationTypes.TRANSLOCATION);

    List<Band> bands = getAllBands(abr);

    AberrationLocationFilter alf = new AberrationLocationFilter();
    alf.createFiltersFor(parentGenomeName, bands, false);

    Scan scan = new Scan();
    scan.setFilter(alf.getFilterList());

    log.info(alf.getFilterLocationList());

    FileSystem fs = FileSystem.get(outputDir.toUri(), new Configuration());

    String derChrName = translocationBands.get(0).getFullName() + "-" + translocationBands.get(1).getFullName();
    Path outputPath =  new Path(outputDir, newGenomeName);
    Path fastaOutput = new Path(outputPath, derChrName);
    if (fs.exists(fastaOutput))
      fs.delete(fastaOutput, true);

    FASTAHeader header = new FASTAHeader(derChrName, newGenomeName, "parent=" + parentGenomeName, abr.getDescription());
    DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(new Configuration(), scan, fastaOutput, alf.getFilterLocationList(), abr, header);
    ToolRunner.run(gdc, null);
    try
      {
      gdc.mergeOutputs(abr.getAberration(), fastaOutput, alf.getFilterLocationList().size());
      }
    catch (Exception e)
      {
      log.error(e);
      }
    }

  private List<Band> getAllBands(Aberration abr)
    {
    List<Band> bands = new ArrayList<Band>();

    Band first = abr.getBands().get(0);
    //    first.setLocation(dao.getBandDAO().getLocation(first));
    //    bands.add(first);

    for(Band b: dao.getBandDAO().getBands(first.getChromosomeName()))
      {
      bands.add(b);
      if (b.getBandName().equals(first.getBandName())) break;
      }

    Band last = abr.getBands().get(abr.getBands().size()-1);
    //    last.setLocation(dao.getBandDAO().getLocation(last));
    //    bands.add(last);

    boolean addBand = false;
    for (Band b : dao.getBandDAO().getBands(last.getChromosomeName()))
      {
      if (b.getBandName().equals(last.getBandName()))
        addBand = true;

      if (addBand)
        bands.add(b);
      }

    return bands;
    }


  }
