import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.BWAIndex;
import org.lcsb.lu.igcsa.DerivativeChromosomeJob;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.karyotype.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.karyotype.database.util.DerbyConnection;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;
import org.lcsb.lu.igcsa.karyotype.generator.BreakpointCombinatorial;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.io.*;
import java.util.*;


/**
 * PACKAGE_NAME
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

/*
This is NOT a general purpose class, but a special karyotype generator for two specific cell lines.
 */
public class SpecialGenerator
  {
  static Logger log = Logger.getLogger(SpecialGenerator.class.getName());

  //private static ClassPathXmlApplicationContext context;
  private static KaryotypeDAO dao;
  private static HBaseGenomeAdmin admin;

  private static GenomeResult parentGenome;
  private static String cellLine;
  private static Path baseOutput;
  private static FileSystem fs;

  private Map<Band, Double> allPossibleBands;
  private String[] chromosomes;
  private List<Candidate> candidates;
  private double min = 1.0;
  private double max = 0.0;


  /*
  I'm not generating whole chromosomes here, instead I'm generating only the bands involved with the breakpoint.  This should give me plenty of room up/down stream
  for aligning, and should make it simpler/faster
   */

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

    //    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml"});
    //    dao = (KaryotypeDAO) context.getBean("karyotypeDAO");
    //derby:classpath
    //    DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver","jdbc:derby:jar:(s3n://insilico/karyotype_probabilities.zip)karyotype_probabilities", "igcsa", "");


    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(HBaseConfiguration.create());
    parentGenome = admin.getGenomeTable().getGenome("GRCh37");
    SpecialGenerator sg = new SpecialGenerator();

    cellLine = cl.getOptionValue("c");

    baseOutput = new Path(cl.getOptionValue("o"), cellLine);
    fs = FileSystem.get(baseOutput.toUri(), new Configuration());
    int i = 1;
    while (fs.exists(baseOutput))
      {
      baseOutput = new Path(cl.getOptionValue("o"), cellLine + "." + i);
      ++i;
      }

    if (cl.hasOption("b"))
      {
      for (String bandPair : cl.getOptionValue("b").split(";"))
        {
        String[] bandNames = bandPair.split(",");

        String fastaName = StringUtils.join(bandNames, "-");
        List<Band> bands = new ArrayList<Band>();
        List<String> chrs = new ArrayList<String>();
        for (String b : bandNames)
          {
          Band band = new Band(b);
          System.out.println("*** BAND: " + band);
          //Location loc = dao.getBandDAO().getLocation(band);
          if (dao.getBandDAO().getBandByChromosomeAndName(band.getChromosomeName(), band.getBandName()) != null)
            {
            band.setLocation(dao.getBandDAO().getLocation(band));
            bands.add(band);
            chrs.add(band.getChromosomeName());
            }
          else
            {
            log.warn("Band " + band + " doesn't exist in database.");
            bands.clear();
            }
          //band.setLocation(dao.getBandDAO().getLocation(band));
          }
        if (bands.size() > 0)
          createDerivativeJob(fastaName, AberrationTypes.TRANSLOCATION, bands, chrs.toArray(new String[chrs.size()]));
        }
      }

    else if (cl.hasOption("f"))
      {
      String fileName = cl.getOptionValue("f");

      InputStream is = fs.open(new Path(fileName));
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      reader.readLine(); // header
      String line;
      while ((line = reader.readLine()) != null)
        {
        String[] cols = line.split("\t");
        if (cols.length < 3)
          continue;

        String chr = cols[0];
        String vagueAbr = cols[2];

        // translocation
        if (vagueAbr.startsWith("t"))
          {
          String[] chrs = vagueAbr.substring(vagueAbr.indexOf("(") + 1, vagueAbr.indexOf(")")).split(";");

          for (int n = 0; n < chrs.length - 1; n++)
            {
            sg.run(chrs[n], chrs[n + 1]);
            int maxCand = (int) (sg.getCandidates().size() * .05); // randomly generate top scoring...
            //Collections.shuffle(sg.getCandidates());
            //for (int j=0;j<maxCand; j++)
            for (Candidate c : sg.getTopCandidates(maxCand))
              {
              //Candidate c = sg.getCandidates().get(j);
              String fastaName = chrs[n] + c.getBands().get(0).getBandName() + "-" + chrs[n + 1] + c.getBands().get(1).getBandName();
              createDerivativeJob(fastaName, AberrationTypes.TRANSLOCATION, c.getBands(), chrs);
              }
            //          if (sg.getCandidates().size() > 0)
            //            createDerivativeJob("der" + chrs[n]+"-"+chrs[n+1], AberrationTypes.TRANSLOCATION, sg.getTopCandidates(1).get(0).getBands(), chrs);
            }
          }
        else if (vagueAbr.startsWith("i")) // isochromosome
          {
          String arm = vagueAbr.replace("i(", "").substring(0, 1);
          String fastaName = "iso" + chr + arm;
          if (arm.equals("q"))
            continue;
          // p11 or q 11
          List<Band> bands = new ArrayList<Band>();
          for (Band b : dao.getBandDAO().getBands(chr))
            if (b.whichArm().equals(arm))
              bands.add(b);
          //        createDerivativeJob(fastaName, AberrationTypes.ISOCENTRIC, bands, chr);
          }
        else if (vagueAbr.equals("del(q/p)"))
          {
          // chromosome missing an arm -- doesn't require the special generator
          for (String arm : new String[]{"p", "q"}) // p11 or q11
            {
            String fastaName = "del" + chr + arm;
            List<Band> bands = new ArrayList<Band>();
            for (Band b : dao.getBandDAO().getBands(chr))
              if (!b.whichArm().equals(arm))
                bands.add(b);
            //createDerivativeJob(fastaName, AberrationTypes.DELETION, bands, chr);
            }
          }
        }
      }
    //System.exit(-1);

    //Create BWA index with ONLY the derivative chromosomes
    //final Path mergedFASTA = new Path(baseOutput, "reference.fa");
    // Create a single merged FASTA file for use in the indexing step
    //FASTAUtil.mergeFASTAFiles(fs, baseOutput.toString(), mergedFASTA.toString());

    // Run BWA
    //Path tmp = BWAIndex.writeReferencePointerFile(mergedFASTA, fs);
    //ToolRunner.run(new BWAIndex(), (String[]) ArrayUtils.addAll(args, new String[]{"-f", tmp.toString()}));
    //fs.delete(tmp, false);
    }

  private static AberrationLocationFilter createFilters(List<Band> bands, Aberration abr, String... chrs) throws IOException
    {
    List<ChromosomeResult> chromosomes = new ArrayList<ChromosomeResult>();
    for (String c : chrs)
      chromosomes.add(admin.getChromosomeTable().getChromosome(parentGenome.getName(), c));
    AberrationLocationFilter alf = new AberrationLocationFilter();

    switch (abr.getAberration())
      {
      case ISOCENTRIC:
        Collections.sort(bands);
        //        int start = bands.get(0).getLocation().getStart();
        //        int end = bands.get(bands.size() - 1).getLocation().getEnd();
        //        alf.createFiltersFor(parentGenome.getName(), new Location(chrs[0], start, end));
        alf.createFiltersFor(parentGenome.getName(), bands.get(bands.size() - 1).getLocation());

        break;
      //      case DELETION:
      //        alf.createFiltersFor(parentGenome.getName(), bands, true);
      //        break;
      default:
        alf.createFiltersFor(parentGenome.getName(), bands, false);
        //alf.getFilter(abr, parentGenome, chromosomes, true);
        break;
      }
    return alf;
    }


  private static void createDerivativeJob(String fastaName, AberrationTypes type, List<Band> bands, String... chrs) throws Exception
    {
    Aberration aberration = new Aberration(bands, type);

    AberrationLocationFilter alf = createFilters(bands, aberration, chrs);
    Scan scan = new Scan();
    scan.setFilter(alf.getFilterList());

    Path fastaOutput = new Path(baseOutput, fastaName);
    if (fs.exists(fastaOutput))
      fs.delete(fastaOutput, true);

    // Generate the segments for the new FASTA file
    List<String> abrs = new ArrayList<String>();
    for (Band band : aberration.getBands())
      abrs.add(band.getChromosomeName() + ":" + band.getLocation().getStart() + "-" + band.getLocation().getEnd());
    String abrDefinitions = aberration.getAberration().getShortName() + ":" + StringUtils.join(abrs.iterator(), ",");

    log.info("*** Running DerivativeChromosomeJob for " + aberration.toString());
    String desc = aberration.getAberration().getCytogeneticDesignation() + " ";
    switch (aberration.getAberration())
      {
      case DELETION:
        desc = fastaName;
        break;
      case ISOCENTRIC:
        desc = desc + " " + bands.get(0).getChromosomeName() + bands.get(0).whichArm();
        break;
      default:
        for (Band b : aberration.getBands())
          desc = desc + b.getChromosomeName() + b.getBandName() + ";";
        desc = desc.substring(0, desc.lastIndexOf(";"));
        break;
      }
    desc = desc + ",bp=" + bands.get(0).getLocation().getLength();

    FASTAHeader header = new FASTAHeader(cellLine, fastaName, "parent=" + parentGenome.getName(), desc);
    DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(new Configuration(), scan, fastaOutput, alf.getFilterLocationList(), aberration, header);
    ToolRunner.run(gdc, null);
    try
      {
      gdc.mergeOutputs(aberration.getAberration(), fastaOutput, alf.getFilterLocationList().size());
      }
    catch (Exception e)
      {
      log.error(e);
      }
    }

  public SpecialGenerator()
    {
    }

  public List<Candidate> getCandidates()
    {
    return candidates;
    }

  public List<Candidate> getTopCandidates(int n)
    {
    Collections.sort(candidates, new Comparator<Candidate>()
    {
    public int compare(Candidate a, Candidate b)
      {
      return Double.compare(b.getScore(), a.getScore());
      }
    });
    return candidates.subList(0, n);
    }

  public void run(String... chrs) throws ProbabilityException
    {
    candidates = new ArrayList<Candidate>();
    allPossibleBands = new HashMap<Band, Double>();
    chromosomes = chrs;

    if (chrs.length >= 4)
      {
      log.warn("Too many chromosomes for breakpoint combinatorial analysis (memory). " + StringUtils.join(chrs, ","));
      return;
      }

    //Map<Object, Double> rawProbs = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities().getRawProbabilities();

    // NOTE I'm pulling probabilities for breakpoints WITHIN the chromosome, not across all chromosomes
    for (String c : chromosomes)
      {
      for (Map.Entry<Object, Double> entry : dao.getGeneralKarytoypeDAO().getBandProbabilities(c).getRawProbabilities().entrySet())
        {
        Band b = (Band) entry.getKey();
        b.setLocation(dao.getBandDAO().getLocation(b));

        allPossibleBands.put(b, entry.getValue());
        }
      }

    // no duplicates
    BreakpointCombinatorial.GENERATOR = BreakpointCombinatorial.SIMPLE_GEN;
    BreakpointCombinatorial combinatorial = new BreakpointCombinatorial();
    List<ICombinatoricsVector<Band>> breakpoints = combinatorial.getCombinations(allPossibleBands.keySet().toArray(new Band[allPossibleBands.size()]), chromosomes.length);

    filterBreakpoints(breakpoints);
    scoreBreakpoints(breakpoints);
    log.info("Breakpoints: " + breakpoints.size());
    log.info("Candidates: " + candidates.size());
    log.info("Min:" + min + " Max: " + max);

    for (int i = 0; i < candidates.size(); i++)
      {
      log.debug(candidates.get(i));
      if (i >= 10)
        break;
      }
    }

  private void scoreBreakpoints(List<ICombinatoricsVector<Band>> breakpoints)
    {
    min = 1.0;
    max = 0.0;

    for (Iterator<ICombinatoricsVector<Band>> bI = breakpoints.iterator(); bI.hasNext(); )
      {
      Candidate cand = new Candidate();
      for (Band b : bI.next())
        cand.addBreakpoint(b, allPossibleBands.get(b));
      candidates.add(cand);
      if (cand.getScore() < min)
        min = cand.getScore();
      if (cand.getScore() > max)
        max = cand.getScore();
      }
    Collections.sort(candidates);
    Collections.reverse(candidates);
    }

  // order matters, I only want sets that are in the order I provided for the chrs
  private List<Band> sortBands(List<Band> bands)
    {
    // order them to chr order
    List<Band> sorted = new ArrayList<Band>();
    for (int i = 0; i < chromosomes.length; i++)
      {
      for (Band b : bands)
        if (b.getChromosomeName().equals(chromosomes[i]))
          sorted.add(b);
      }
    return sorted;
    }

  // At this point it's also looking like I should make sure I never include the same band in any two different aberrations.  So even though 10q24 scores highly, it shouldn't combine more than once with another band from the same chromosome.
  private void filterBreakpoints(List<ICombinatoricsVector<Band>> breakpoints)
    {
    for (Iterator<ICombinatoricsVector<Band>> bI = breakpoints.iterator(); bI.hasNext(); )
      {
      boolean remove = false;
      ICombinatoricsVector<Band> vector = bI.next();

      List<Band> bands = sortBands(vector.getVector());
      // going to get rid of centromeres entirely right now, they are not gene-rich regions and they are highly probable so bias the results
      for (Band b : bands)
        {
        if (b.isCentromere())
          {
          remove = true;
          bI.remove();
          break;
          }
        }

      // get rid of vectors that involve only a single chromosome
      if (!remove)
        {
        String lastChr = "";
        for (Band b : bands)
          {
          if (b.getChromosomeName().equals(lastChr))
            {
            remove = true;
            bI.remove();
            break;
            }
          lastChr = b.getChromosomeName();
          }
        }

      // Avoid iso type chromomsomes
      if (!remove)
        {
        int centCnt = 0;
        for (Band b : vector.getVector())
          if (b.isCentromere())
            ++centCnt;

        if (centCnt >= 2)
          bI.remove();
        }

      for (int i = 0; i < bands.size(); i++)
        vector.setValue(i, bands.get(i));
      }
    }

  static class Candidate implements Comparable<Candidate>
    {
    private List<Band> bands = new ArrayList<Band>();
    private double probScore = 0.0;

    public void addBreakpoint(Band b, double p)
      {
      bands.add(b);
      probScore += p;
      probScore = (double) Math.round(probScore * 10000) / 10000;
      }

    public List<Band> getBands()
      {
      return bands;
      }

    public double getScore()
      {
      return probScore;
      }

    public String toString()
      {
      return "[" + bands.toString() + ", " + probScore + "]";
      }

    public int compareTo(Candidate candidate)
      {
      return Double.compare(this.getScore(), candidate.getScore());
      }
    }

  }
