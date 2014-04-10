import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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
  private Map<Band, Double> allPossibleBands;
  private String[] chromosomes;
  private List<Candidate> candidates;
  private double min = 1.0;
  private double max = 0.0;

  public static void main(String[] args) throws Exception
    {
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(HBaseConfiguration.create());

    SpecialGenerator sg = new SpecialGenerator();

    String fileName = args[0];
    InputStream is = new FileInputStream(new File(fileName));
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    reader.readLine(); // header
    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] cols = line.split("\t");
      if (cols.length < 3)
        continue;

      //log.info(line);
      String chr = cols[0];
      String vagueAbr = cols[2];

      // translocation
      if (vagueAbr.startsWith("t"))
        {
        if (vagueAbr.split("\\)\\(").length > 1)
          {
          // there's an actual definition
          }
        else
          {
          vagueAbr = vagueAbr.replaceFirst("t", "").replace("(", "").replace(")", "");
          String[] chrs = vagueAbr.split(";");
          sg.run(chrs);

          if (sg.getCandidates().size() <= 0)
            continue;


          GenomeResult parentGenome = admin.getGenomeTable().getGenome("GRCh37");
          List<ChromosomeResult> chromosomes = new ArrayList<ChromosomeResult>();
          for (String c: chrs)
            chromosomes.add(admin.getChromosomeTable().getChromosome("GRCh37", c));

          Aberration aberration = new Aberration(sg.getTopCandidates(1).get(0).getBands(), AberrationTypes.TRANSLOCATION);
          AberrationLocationFilter alf = new AberrationLocationFilter();
          FilterList filterList = alf.getFilter( aberration, parentGenome, chromosomes);

//          filterList = new FilterList();
//          filterList.addFilter( new RowFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("AAAA00024002:1-GRCh37"))) );

          Scan scan = new Scan();
          scan.setFilter(filterList);

          String fastaName = "der" + StringUtils.join(chrs, "-");
          Path baseOutput = new Path("/tmp/special/HCC1594-1", fastaName);

          // Generate the segments for the new FASTA file
          List<String> abrs = new ArrayList<String>();
          for (Band band : aberration.getBands() )// aberration.getAberrationDefinitions())
            abrs.add(band.getChromosomeName() + ":" + band.getLocation().getStart() + "-" + band.getLocation().getEnd());
          String abrDefinitions = aberration.getAberration().getCytogeneticDesignation() + ":" + StringUtils.join(abrs.iterator(), ",");

          log.info("*** Running DerivativeChromosomeJob for " + aberration.toString());
          FASTAHeader header = new FASTAHeader("HCC1594-1", fastaName, "parent=" + parentGenome.getName(), abrDefinitions);
          DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(new Configuration(), scan, baseOutput, alf.getFilterLocationList(), aberration.getAberration().getCytogeneticDesignation(), header);
          ToolRunner.run(gdc, null);
          gdc.mergeOutputs(aberration.getAberration(), baseOutput, alf.getFilterLocationList().size());
          }
        }
      else if (vagueAbr.startsWith("i"))
        {
        // isochromosome
        if (vagueAbr.contains("p"))
          {
          // p11
          }
        else
          {
          // q11
          }
        }
      else if (vagueAbr.equals("del(q/p)"))
        {
        // chromosome missing an arm
        }

      }


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
    if (chrs.length >= 4)
      {
      log.warn("Too many chromosomes for breakpoint combinatorial analysis (memory).");
      return;
      }

    allPossibleBands = new HashMap<Band, Double>();
    chromosomes = chrs;

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
    candidates = new ArrayList<Candidate>();
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
