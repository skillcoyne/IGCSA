import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.DerivativeChromosomeJob;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.BreakpointCombinatorial;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

  private static ClassPathXmlApplicationContext context;
  private static KaryotypeDAO dao;
  private static HBaseGenomeAdmin admin;

  private static GenomeResult parentGenome;
  private static String cellLine;


  private Map<Band, Double> allPossibleBands;
  private String[] chromosomes;
  private List<Candidate> candidates;
  private double min = 1.0;
  private double max = 0.0;


  public static void main(String[] args) throws Exception
    {
    if (args.length < 2)
      {
      System.err.println("File name and cell line name required.");
      System.exit(-1);
      }

    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(HBaseConfiguration.create());
    parentGenome = admin.getGenomeTable().getGenome("GRCh37");
    SpecialGenerator sg = new SpecialGenerator();

    String fileName = args[0];
    cellLine = args[1];

    InputStream is = new FileInputStream(new File(fileName));
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
        sg.run(chrs);
        if (sg.getCandidates().size() > 0)
          createDerivativeJob("der" + StringUtils.join(chrs, "-"), AberrationTypes.TRANSLOCATION, sg.getTopCandidates(1).get(0).getBands(), chrs);
        }
      else if (vagueAbr.startsWith("i")) // isochromosome NOT YET IMPLEMENTED -- doesn't require the special generator
        {
        String arm = vagueAbr.replace("i(", "").substring(0, 1);
        String fastaName = "iso" + chr + arm;
        if (arm.equals("p")) continue;
        // p11 or q 11
        List<Band> bands = new ArrayList<Band>();
        for (Band b : dao.getBandDAO().getBands(chr))
          if (b.whichArm().equals(arm))
            bands.add(b);
        createDerivativeJob(fastaName, AberrationTypes.ISOCENTRIC, bands, chr);
        }
      else if (vagueAbr.equals("del(q/p)"))
        {
        // chromosome missing an arm -- doesn't require the special generator
        for (String arm : new String[]{"p","q"}) // p11 or q11
          {
          String fastaName = "del" + chr + arm;
          List<Band> bands = new ArrayList<Band>();
          for (Band b : dao.getBandDAO().getBands(chr))
            if (!b.whichArm().equals(arm))
              bands.add(b);
          createDerivativeJob(fastaName, AberrationTypes.DELETION, bands, chr);
          }
        }
      }
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
        alf.createFiltersFor(parentGenome.getName(), new Location(chrs[0], bands.get(0).getLocation().getStart(), bands.get(bands.size()-1).getLocation().getEnd())); break;
      case DELETION:
        alf.createFiltersFor(parentGenome.getName(), bands, true); break;
      default:
        alf.getFilter(abr, parentGenome, chromosomes, true); break;
      }
    return alf;
    }


  private static void createDerivativeJob(String fastaName, AberrationTypes type, List<Band> bands, String... chrs) throws Exception
    {
    Aberration aberration = new Aberration(bands, type);

    AberrationLocationFilter alf = createFilters(bands, aberration, chrs);
    Scan scan = new Scan();
    scan.setFilter(alf.getFilterList());

    Path baseOutput = new Path("/tmp/special/" + cellLine, fastaName);
    FileSystem fs = FileSystem.get(baseOutput.toUri(), new Configuration());
    if (fs.exists(baseOutput))
      fs.delete(baseOutput, true);

    // Generate the segments for the new FASTA file
    List<String> abrs = new ArrayList<String>();
    for (Band band : aberration.getBands())
      abrs.add(band.getChromosomeName() + ":" + band.getLocation().getStart() + "-" + band.getLocation().getEnd());
    String abrDefinitions = aberration.getAberration().getShortName() + ":" + StringUtils.join(abrs.iterator(), ",");

    log.info("*** Running DerivativeChromosomeJob for " + aberration.toString());
    FASTAHeader header = new FASTAHeader(cellLine, fastaName, "parent=" + parentGenome.getName(), aberration.toString());
    DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(new Configuration(), scan, baseOutput, alf.getFilterLocationList(), aberration.getAberration().getCytogeneticDesignation(), header);
    ToolRunner.run(gdc, null);
    gdc.mergeOutputs(aberration.getAberration(), baseOutput, alf.getFilterLocationList().size());
    }

  public SpecialGenerator()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml"});
    dao = (KaryotypeDAO) context.getBean("karyotypeDAO");
    }

  public List<Candidate> getCandidates()
    {
    return candidates;
    }

  public List<Candidate> getTopCandidates(int n)
    {
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

  private void filterBreakpoints(List<ICombinatoricsVector<Band>> breakpoints)
    {
    for (Iterator<ICombinatoricsVector<Band>> bI = breakpoints.iterator(); bI.hasNext(); )
      {
      boolean remove = false;
      ICombinatoricsVector<Band> vector = bI.next();

      List<Band> bands = sortBands(vector.getVector());
      // it starts with q, ends with p.  I will tend to get a lot of p11 at the start for multiple translocation chrs (>2)
      if (!bands.get(0).whichArm().equals("q") || !bands.get(bands.size() - 1).whichArm().equals("p"))
        {
        remove = true;
        bI.remove();
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
