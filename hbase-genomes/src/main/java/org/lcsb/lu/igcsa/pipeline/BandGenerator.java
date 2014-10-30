/**
 * org.lcsb.lu.igcsa.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.karyotype.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.karyotype.database.util.DerbyConnection;
import org.lcsb.lu.igcsa.karyotype.generator.BreakpointCombinatorial;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.io.IOException;
import java.util.*;


public class BandGenerator
  {
  private static final Log log = LogFactory.getLog(BandGenerator.class);

  private static KaryotypeDAO dao;

  private ArrayList<Candidate> candidates;
  private HashMap<Band, Double> allPossibleBands;
  private String[] chromosomes;
  private Set<Band> bandsInSet;


  private double min = 1.0;
  private double max = 0.0;

  public static boolean FILTER_CENT = true;



  public BandGenerator() throws IOException
    {
    if (dao == null)
      {
      DerbyConnection conn = new DerbyConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:classpath:karyotype_probabilities", "igcsa", "");
      dao = conn.getKaryotypeDAO();
      }
    }

  public List<Candidate> getCandidates()
    {
    return candidates;
    }

  public List<Candidate> getTopCandidates(int n)
    {
    if (candidates.size() < n) return candidates;

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

  // At this point it's also looking like I should make sure I never include the same band in any two different aberrations.  So even though 10q24 scores highly, it shouldn't combine more than once with another band from the same chromosome.
  private void filterBreakpoints(List<ICombinatoricsVector<Band>> breakpoints)
    {
    this.bandsInSet = new HashSet<Band>();
    for (Iterator<ICombinatoricsVector<Band>> bI = breakpoints.iterator(); bI.hasNext(); )
      {
      boolean remove = false;
      ICombinatoricsVector<Band> vector = bI.next();

      List<Band> bands = vector.getVector();
      // going to get rid of centromeres entirely right now, they are not gene-rich regions and they are highly probable so bias the results
      // also filter out any that have a band already seen.  It's going to match regardless
      for (Band b : bands)
        {
        if (FILTER_CENT && b.isCentromere())
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
          {
          bI.remove();
          remove = true;
          }
        }

      if (!remove)
        {
        if (duplicateBand(bands))
          bI.remove();
        else
          this.bandsInSet.addAll(bands);
        }
      }
    }

  private boolean duplicateBand(List<Band> bands)
    {
    for (Band b: bands)
      {
      if (this.bandsInSet.contains(b))
        return true;
      }
    return false;
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
