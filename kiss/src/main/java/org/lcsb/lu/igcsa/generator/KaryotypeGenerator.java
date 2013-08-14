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
  private Properties karyotypeProperties;

  private Karyotype karyotype;

  private List<String> chromosomeGains = new ArrayList<String>();
  private List<String> chromosomeLosses = new ArrayList<String>();

  private Set<Band> breakpoints = new HashSet<Band>();
  private Map<String, BandCollection> chromosomes = new HashMap<String, BandCollection>();

  @Autowired
  private AberrationRules aberrationRules;

  // TODO this should NOT be hardwired
  private Map<String, Class<?>> aberrationClasses = new HashMap<String, Class<?>>();


  public KaryotypeGenerator(KaryotypeDAO karyotypeDAO)
    {
    this.karyotypeDAO = karyotypeDAO;
    abrClasses();
    }

  public KaryotypeGenerator(Properties karyotypeProperties)
    {
    this.karyotypeProperties = karyotypeProperties;
    abrClasses();
    }

  public void setAberrationRules(AberrationRules aberrationRules)
    {
    this.aberrationRules = aberrationRules;
    }

  private void abrClasses()
    {
    try
      {
      aberrationClasses.put("trans", Class.forName("org.lcsb.lu.igcsa.aberrations.Translocation"));
      aberrationClasses.put("del", Class.forName("org.lcsb.lu.igcsa.aberrations.Deletion"));
      aberrationClasses.put("dup", Class.forName("org.lcsb.lu.igcsa.aberrations.Duplication"));
      aberrationClasses.put("inv", Class.forName("org.lcsb.lu.igcsa.aberrations.Inversion"));
      }
    catch (ClassNotFoundException e)
      {
      e.printStackTrace();
      }

    }


  /*
 For testing purposes at the moment
  */
  protected void generateKaryotypes(Karyotype kt, Band[] bands)
    {
    aberrationRules.applyRules(bands);

    // apply ploidy
    for (String chr : this.chromosomeGains)
      kt.chromosomeGain(chr);
    for (String chr : this.chromosomeLosses)
      kt.chromosomeLoss(chr);

    // create -n- karyotypes based on the generated aberrations just to allow for some diversity. This can be setable later.
    for (int n = 0; n < 3; n++)
      {
      }

    log.info(aberrationRules.getOrderedBreakpointSets());
    log.info(aberrationRules.getOrderedAberrationSets().size());

    int abrCount = 4;//;new RandomRange(1, aberrationRules.getOrderedAberrationSets().size()).nextInt();

    log.info(abrCount);

    Band[] orderedBands = aberrationRules.getOrderedBreakpointSets().keySet().toArray( new Band[aberrationRules.getOrderedBreakpointSets().size()] ) ;

    Set<Integer> indecies = new HashSet<Integer>();
    for (int i=0; i<abrCount; i++)
      {
      int index = new Random().nextInt(abrCount);

      while (!indecies.contains(index))
        index = new Random().nextInt(abrCount);

      List<ICombinatoricsVector<Aberration>> abrPerBand = aberrationRules.getOrderedBreakpointSets().get(orderedBands[index]);
      int abrSelector = new RandomRange(-1, abrPerBand.size()).nextInt();
      if (abrSelector > 0)
        {
        //abrPerBand.get(abrSelector).getVector();
        }

      }




    for (Map.Entry<Object, List<ICombinatoricsVector<Band>>> entry : aberrationRules.getOrderedAberrationSets().entrySet())
      {
//      log.info(entry);
//      log.info(entry.getValue().size());

      int selector = new Random().nextInt(entry.getValue().size()) - 1;

      if (selector > 0)
        {
//        log.info("---" + entry.getKey() + "---");
//        log.info(entry.getValue().get(selector));
//
//        kt.addAberration(entry.getKey(), entry.getValue().get(selector).getVector() );

        }
      }

    //    log.info(aberrationRules.getOrderedBreakpointSets().keySet());
    //    log.info(aberrationRules.getOrderedBreakpointSets().size());
    //
    //    log.info(aberrationRules.getAberrations().size());
    //
    //    for (Map.Entry<List<Band>, List<ICombinatoricsVector<Aberration>>> entry: aberrationRules.getOrderedBreakpointSets().entrySet())
    //      {
    //      log.info("---" + entry.getKey() + "---");
    //      log.info(entry.getValue().get( new Random().nextInt(entry.getValue().size()) ));
    //      }


    }


  /*
  ---- 1 ----
  Per karyotype probabilities
   (A) select the number of chromosomes that should have breakpoints
   (B) select the number of aneuploidy's that should occur
   (C) select the number of breakpoints that should occur per chromosome
  */
  public void generateKaryotypes(Karyotype kt) throws ProbabilityException
    {
    setUp();
    createAberrations();
    //List<Karyotype> karyotypeList = new ArrayList<Karyotype>(aberrations.size());

    List<ICombinatoricsVector<Aberration>> aberrations = aberrationRules.getAberrations();

    log.info(aberrationRules.getOrderedBreakpointSets().keySet());
    log.info(aberrationRules.getOrderedBreakpointSets().size());

    log.info(aberrations.size());

    //    ICombinatoricsVector<ICombinatoricsVector<Aberration>> initialVector = Factory.createVector(aberrations);
    //    Generator<ICombinatoricsVector<Aberration>> gen = Factory.createMultiCombinationGenerator(initialVector, aberrationRules.getOrderedBreakpointSets().size()); // this one will include sets of same objects (foo, foo)
    //
    //    for (ICombinatoricsVector<ICombinatoricsVector<Aberration>> vector: gen.generateAllObjects())
    //      {
    //      log.info(vector);
    //      }


    }


  /*
  This will likely need to be rule-based. So what are the rules?
  */
  private void createAberrations()
    {
    aberrationRules.applyRules(breakpoints.toArray(new Band[breakpoints.size()]));
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
      chromosomes.put(chr, new BandCollection((Collection<Band>) bands));
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
