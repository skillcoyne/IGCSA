/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.dist;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.util.*;

public class KaryotypeGenerator
  {
  static Logger log = Logger.getLogger(KaryotypeGenerator.class.getName());

  private KaryotypeDAO karyotypeDAO;
  private Properties karyotypeProperties;

  private List<String> chromosomeGains = new ArrayList<String>();
  private List<String> chromosomeLosses = new ArrayList<String>();
  private Set<String> breakpoints = new HashSet<String>();

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
    /* This occurs in several steps:
    ---- 1 ----  Per karyotype probabilities
    (A) select the number of aberrations that should occur
    (B) select the number of aneuploidy's that should occur
    (C) select the number of breakpoints that should occur???
    */

    // 1 Select number of chromosomes that are unstable
    Probability chrNums = new Probability(  // this should probably go into the database
        new Object[] {0,       1,       2,       3,       4,       5,       6,       7,       8,       9,       10,      11,     12,       13,      14,      15,      16,     17,   18,19,20,21,22},
        new double[] {0.47133, 0.10644, 0.27060, 0.05352, 0.04465, 0.01981, 0.01320, 0.00828, 0.00481, 0.00335, 0.00199, 0.00022, 0.00022, 0.00022, 0.00022, 0.00022, 0.00022, 0.00022,0.00022, 0.00022, 0.00022, 0.00022, 0.00022}
    );

    Integer chrCount = (Integer) chrNums.roll();
    int totalPloidy = selectCount("aneuploidy");

    while (chrCount <= 0 || totalPloidy <= 0) // one of these has to be positive to make this worthwhile
      {
      if (chrCount<= 0) chrCount = (Integer) chrNums.roll();
      if (totalPloidy <= 0) totalPloidy = selectCount("aneuploidy");
      }

    log.info("Total aneuploidy: " + totalPloidy);
    getAneuploidy(totalPloidy);



    log.info("Select " + chrCount + " chromosomes");
    List<String> chromosomeList = new ArrayList<String>();
    for (int i=1; i <= chrCount; i++)
      chromosomeList.add((String) karyotypeDAO.getGeneralKarytoypeDAO().getChromosomeInstability().roll());
    log.info("Chromosomes: " + chromosomeList);

    getBreakpoints(chromosomeList);
    }

  private void getBreakpoints(List<String> chromosomes) throws ProbabilityException
    {
    for (String chr: chromosomes)
      {
      int r = new RandomRange(1,3).nextInt(); // no good way yet to decide how many breaks should occur
      log.info(r + " bps for " + chr);
      for (int i=1; i<=r; i++)
        breakpoints.add(chr + (String) karyotypeDAO.getGeneralKarytoypeDAO().getBandInstability(chr).roll());
      }
    log.info(breakpoints);

    log.info("Centromere: " + karyotypeDAO.getGeneralKarytoypeDAO().getCentromereInstability().roll().toString());

    }


  private void getAneuploidy(int totalChromosomes) throws ProbabilityException
    {
    for (int i=1; i<= totalChromosomes; i++)
      {
      String chromosome = (String) karyotypeDAO.getAneuploidyDAO().getChromosomeProbabilities().roll();
      String ploidy = (String) karyotypeDAO.getAneuploidyDAO().getGainLoss(chromosome).roll();
      if (ploidy.equals("gain")) chromosomeGains.add(chromosome);
      else chromosomeLosses.add(chromosome);
      }
    log.info("Gains: " + this.chromosomeGains.toString() + " Losses: " + this.chromosomeLosses.toString());
    }


  private int selectCount(String abtype) throws ProbabilityException
    {
    IntRange abrRange = (IntRange) karyotypeDAO.getGeneralKarytoypeDAO().getProbabilityClass(abtype).roll();
    return new RandomRange(abrRange).nextInt();
    }



  }
