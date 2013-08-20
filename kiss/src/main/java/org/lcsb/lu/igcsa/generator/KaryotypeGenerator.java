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
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class KaryotypeGenerator
  {
  static Logger log = Logger.getLogger(KaryotypeGenerator.class.getName());

  private KaryotypeDAO karyotypeDAO;

  private Karyotype karyotype;

  private List<String> chromosomeGains = new ArrayList<String>();
  private List<String> chromosomeLosses = new ArrayList<String>();

  private Set<Band> breakpoints = new HashSet<Band>();
  private Map<String, BandCollection> chromosomes = new HashMap<String, BandCollection>();

  @Autowired
  private AberrationRules aberrationRules;

  public KaryotypeGenerator(KaryotypeDAO karyotypeDAO)
    {
    this.karyotypeDAO = karyotypeDAO;
    }

  public void setAberrationRules(AberrationRules aberrationRules)
    {
    this.aberrationRules = aberrationRules;
    }


  protected Karyotype generateKaryotype(Karyotype kt, Band[] bands)
    {
    aberrationRules.applyRules(bands);

    // apply ploidy
    for (String chr : this.chromosomeGains)
      kt.gainChromosome(chr);
    for (String chr : this.chromosomeLosses)
      kt.loseChromosome(chr);

    if (aberrationRules.getOrderedBreakpointSets().size() > 0)
      {
      int abrCount = new RandomRange(1, aberrationRules.getOrderedAberrationSets().size()).nextInt();

      // Happens when there's a single band.  Multiple aberration types are possible but each band can only have one right now
      if (abrCount > aberrationRules.getOrderedBreakpointSets().size())
        abrCount = aberrationRules.getOrderedBreakpointSets().size();

      List<List<Band>> orderedBands = new ArrayList<List<Band>>(aberrationRules.getOrderedBreakpointSets().keySet());
      // get random selection of aberrations by band
      Collections.shuffle(orderedBands);
      log.debug("ordered bands " + orderedBands);

      for (int i = 0; i < abrCount; i++)
        {
        Collections.shuffle(orderedBands);

        List<Band> bandList = orderedBands.get(0);
        List<ICombinatoricsVector<Aberration>> abrPerBand = aberrationRules.getOrderedBreakpointSets().get(bandList);

        Aberration abr = abrPerBand.get(new Random().nextInt(abrPerBand.size())).getVector().get(0);
        kt.addAberrationDefintion(abr);

        orderedBands.remove(0);
        }
      }
    return kt;
    }

  /*
  ---- 1 ----
  Per karyotype probabilities
   (A) select the number of chromosomes that should have breakpoints
   (B) select the number of aneuploidy's that should occur
   (C) select the number of breakpoints that should occur per chromosome
  */
  public Karyotype generateKaryotypes(Karyotype kt) throws ProbabilityException
    {
    getNumericAberrations();
    return generateKaryotype(kt, breakpoints.toArray(new Band[breakpoints.size()]));
    }

  private void getNumericAberrations() throws ProbabilityException
    {
    int chrCount = selectCount("chromosome"); // 1(A)
    int totalPloidy = selectCount("aneuploidy"); // 1(B)

    while (chrCount <= 0 && totalPloidy <= 0) // one of these has to be positive to make this worthwhile
      {
      if (chrCount <= 0) chrCount = selectCount("chromosome");
      if (totalPloidy <= 0) totalPloidy = selectCount("aneuploidy");
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
    log.debug("getBreakpoints");
    for (String chr : chromosomes.keySet())
      {
      Set<Band> bands = new HashSet<Band>();

      int r = new RandomRange(1, 3).nextInt(); // no good way yet to decide how many breaks should occur
      log.debug(r + " bps for " + chr);
      int i = 1;
      while (i <= r)
        {
        String bandName = (String) karyotypeDAO.getGeneralKarytoypeDAO().getBandProbabilities(chr).roll();

        Band band = karyotypeDAO.getBandDAO().getBandByChromosomeAndName(chr, bandName);
        log.debug(band);
        breakpoints.add(band);
        bands.add(band);
        i++;
        }
      chromosomes.put(chr, new BandCollection((Collection<Band>) bands));
      }
    log.debug("Breakpoints: " + breakpoints);
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
      if (ploidy.equals("gain")) chromosomeGains.add(chromosome);
      else chromosomeLosses.add(chromosome);
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
