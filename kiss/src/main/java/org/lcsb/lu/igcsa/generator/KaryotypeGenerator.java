/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.*;

public class KaryotypeGenerator
  {
  static Logger log = Logger.getLogger(KaryotypeGenerator.class.getName());

  private KaryotypeDAO karyotypeDAO;
  private Properties karyotypeProperties;

  private List<String> chromosomeGains = new ArrayList<String>();
  private List<String> chromosomeLosses = new ArrayList<String>();

  private Set<Band> breakpoints = new HashSet<Band>();
  private Map<String, BandCollection> chromosomes = new HashMap<String, BandCollection>();

  public KaryotypeGenerator(KaryotypeDAO karyotypeDAO)
    {
    this.karyotypeDAO = karyotypeDAO;
    }

  public KaryotypeGenerator(Properties karyotypeProperties)
    {
    this.karyotypeProperties = karyotypeProperties;
    }

  public void generateKaryotype() throws ProbabilityException
    {
    /*
    ---- 1 ----
    Per karyotype probabilities
     (A) select the number of chromosomes that should have breakpoints
     (B) select the number of aneuploidy's that should occur
     (C) select the number of breakpoints that should occur per chromosome
    */
    setUp();
    createAberrations();
    }

  /*
   This will likely need to be rule-based. So what are the rules?
   */
  private void createAberrations()
    {
    AberrationRules rules = new AberrationRules();
    rules.applyRules(breakpoints.toArray(new Band[breakpoints.size()]));

    for(ICombinatoricsVector<Aberration> vector: rules.getAberrations())
      {
      log.info(vector.getVector());
      }
    }


  private void setUp() throws ProbabilityException
    {
    int chrCount = selectCount("chromosome"); // 1(A)
    int totalPloidy = selectCount("aneuploidy"); // 1(B)
    while (chrCount <= 0 || totalPloidy <= 0) // one of these has to be positive to make this worthwhile
      {
      if (chrCount <= 0)
        chrCount = selectCount("chromosome");
      if (totalPloidy <= 0)
        totalPloidy = selectCount("aneuploidy");
      }

    // 1(B)
    log.info("Total aneuploidy: " + totalPloidy);
    getAneuploidy(totalPloidy);

    // continuation of 1(A)
    log.info("Select " + chrCount + " chromosomes");

    for (int i = 1; i <= chrCount; i++)
      chromosomes.put((String) karyotypeDAO.getGeneralKarytoypeDAO().getChromosomeInstability().roll(), null);
    log.info("Chromosomes: " + chromosomes.keySet());

    // 1(C)
    getBreakpoints();
    }

  /*
  Select breakpoints based on provided probabilities for each chromosome in the list
   */
  private void getBreakpoints() throws ProbabilityException
    {
    for (String chr : chromosomes.keySet())
      {
      Set<Band> bands = new HashSet<Band>();

      int r = new RandomRange(1, 3).nextInt(); // no good way yet to decide how many breaks should occur
      log.info(r + " bps for " + chr);
      int i = 1;
      while (i <= r)
        {
        String band = (String) karyotypeDAO.getGeneralKarytoypeDAO().getBandProbabilities(chr).roll();
        {
        breakpoints.add(new Band(chr, band));
        bands.add(new Band(chr, band));
        i++;
        }
        }
      chromosomes.put(chr, new BandCollection( (Collection<Band>) bands));
      }
    log.info("Breakpoints: " + breakpoints);
    }

  /*
  Select the specific chromosomes that should be gained/lost
   */
  private void getAneuploidy(int totalChromosomes) throws ProbabilityException
    {
    for (int i = 1; i <= totalChromosomes; i++)
      {
      String chromosome = (String) karyotypeDAO.getAneuploidyDAO().getChromosomeProbabilities().roll();
      String ploidy = (String) karyotypeDAO.getAneuploidyDAO().getGainLoss(chromosome).roll();
      if (ploidy.equals("gain"))
        chromosomeGains.add(chromosome);
      else
        chromosomeLosses.add(chromosome);
      }
    log.info("Gains: " + this.chromosomeGains.toString() + " Losses: " + this.chromosomeLosses.toString());
    }

  /*
  For the probabilities that are given in ranges (number of chromosomes that break, number of aneuploidy's)
   */
  private int selectCount(String abtype) throws ProbabilityException
    {
    IntRange abrRange = (IntRange) karyotypeDAO.getGeneralKarytoypeDAO().getProbabilityClass(abtype).roll();
    return new RandomRange(abrRange).nextInt();
    }


  }
