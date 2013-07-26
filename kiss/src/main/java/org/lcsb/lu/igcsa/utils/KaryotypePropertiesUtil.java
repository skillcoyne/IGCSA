package org.lcsb.lu.igcsa.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.aberrations.Aberration;
import org.lcsb.lu.igcsa.aberrations.Duplication;
import org.lcsb.lu.igcsa.aberrations.Translocation;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.ChromosomeBandDAO;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

/**
 * Reads the karyotype.properties file, returns list of Aberration objects
 */
public class KaryotypePropertiesUtil
  {
  static Logger log = Logger.getLogger(KaryotypePropertiesUtil.class.getName());

  // ridiculous, but temporary
  public static Object[] getTranslocations(ChromosomeBandDAO bandDAO, Properties properties, Map<String, Chromosome> chromosomeMap) throws Exception
    {
    List<Aberration> translocationList = new ArrayList<Aberration>();

    Set<String> chromosomes = new LinkedHashSet<String>();
    for (String abrType : properties.stringPropertyNames())
      {
      Aberration AbrClass = (Aberration) Class.forName(abrType).newInstance();

      String aberrations = properties.getProperty(abrType);

      for (String abr : aberrations.split(","))
        {
        // TRANSLOCATION
        if (AbrClass.getClass().getName().equals(Translocation.class.getName()))
          { //14(q32)->11(q13)
          Translocation trans = (Translocation) AbrClass;
          for (String breakpoint : abr.split("->"))
            {
            String[] chrBand = getChrBand(breakpoint);
            Band band = bandDAO.getBandByChromosomeAndName(chrBand[0], chrBand[1]);

            trans.addFragment(new ChromosomeFragment(band.getChromosomeName(), band.getBandName(), band.getLocation()), chromosomeMap.get(band.getChromosomeName()));

            chromosomes.add(band.getChromosomeName());
            }
          translocationList.add(trans);
          }
        }
      }
    return (translocationList.size() > 0) ? (new Object[]{chromosomes, translocationList}) : null;
    }

  public static Map<String, List<Aberration>> getAberrationList(ChromosomeBandDAO bandDAO, Properties properties) throws Exception
    {
    Map<String, List<Aberration>> chrAbrMap = new HashMap<String, List<Aberration>>();

    for (String abrType : properties.stringPropertyNames())
      {
      Aberration AbrClass = (Aberration) Class.forName(abrType).newInstance();

      String aberrations = properties.getProperty(abrType);

      for (String abr : aberrations.split(","))
        {
        if (AbrClass.getClass().getName().equals(Translocation.class.getName()))
          continue;

        if (AbrClass.getClass().getName().equals(Duplication.class.getName()))
          { //12(q13:q22)
          String chr = abr.substring(0, abr.indexOf("("));
          String[] bands = abr.substring(abr.indexOf("(") + 1, abr.indexOf(")")).split(":");

          Band band = bandDAO.getBandByChromosomeAndName(chr, bands[0]);
          Location loc1 = band.getLocation();

          band = bandDAO.getBandByChromosomeAndName(chr, bands[1]);
          Location loc2 = band.getLocation();

          AbrClass.addFragment(new ChromosomeFragment(chr, StringUtils.join(bands, ""), new Location(loc1.getStart(), loc2.getEnd())));

          if (!chrAbrMap.containsKey(chr))
            chrAbrMap.put(chr, new ArrayList<Aberration>());

          chrAbrMap.get(chr).add(AbrClass);
          }
        else
          {
          String[] chrBand = getChrBand(abr);
          Band band = bandDAO.getBandByChromosomeAndName(chrBand[0], chrBand[1]);
          AbrClass.addFragment(new ChromosomeFragment(band.getChromosomeName(), band.getBandName(), band.getLocation()));

          if (!chrAbrMap.containsKey(band.getChromosomeName()))
            chrAbrMap.put(band.getChromosomeName(), new ArrayList<Aberration>());

          chrAbrMap.get(band.getChromosomeName()).add(AbrClass);
          }
        }
      }

    return chrAbrMap;
    }

  private static String[] getChrBand(String s)
    {
    String chr = s.substring(0, s.indexOf("("));
    String band = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
    return new String[]{chr, band};
    }

  public static Probability temporaryGetChromosomeProbabilities()
    {
    TreeMap<Object, Double> probs = new TreeMap<Object, Double>();

    probs.put("1", 0.0467956972956594);
    probs.put("2", 0.0193356563896674);
    probs.put("3", 0.0314559503067949);
    probs.put("4", 0.0172430118930384);
    probs.put("5", 0.0311718809181123);
    probs.put("6", 0.0281607453980759);
    probs.put("7", 0.0576092720248466);
    probs.put("8", 0.0297231270358306);
    probs.put("9", 0.0882224831452163);
    probs.put("10", 0.0285489735626089);
    probs.put("11", 0.0775320051511249);
    probs.put("12", 0.092616089690175);
    probs.put("13", 0.0200079539428831);
    probs.put("14", 0.0422127111582456);
    probs.put("15", 0.0353287629725021);
    probs.put("16", 0.0331414286796455);
    probs.put("17", 0.0769165214756458);
    probs.put("18", 0.0226403302780092);
    probs.put("19", 0.0252632376335126);
    probs.put("20", 0.0508484205741989);
    probs.put("21", 0.0944814786758579);
    probs.put("22", 0.0391636997197182);
    probs.put("X", 0.0115805620786304);

    try
      {
      Probability p = new Probability(probs);
      return p;
      }
    catch (ProbabilityException e)
      {
      log.error(e);
      }
    return null;
    }

  public static Probability temporaryGetPloidyProbability()
    {
    TreeMap<Object, Double> probs = new TreeMap<Object, Double>();

    probs.put("1", 0.0223958598888492);
    probs.put("10", 0.0349321878345995);
    probs.put("11", 0.0315415795645745);
    probs.put("12", 0.047889155152195);
    probs.put("13", 0.0464742772650793);
    probs.put("14", 0.0357670932544741);
    probs.put("15", 0.0364554122265844);
    probs.put("16", 0.0306939274970683);
    probs.put("17", 0.0418663641462295);
    probs.put("18", 0.0446260133584867);
    probs.put("19", 0.0395464742772651);
    probs.put("2", 0.0238680976903074);
    probs.put("20", 0.037991383266201);
    probs.put("21", 0.050304644878397);
    probs.put("22", 0.0429625758425534);
    probs.put("3", 0.0340590424718299);
    probs.put("4", 0.0274626523224392);
    probs.put("5", 0.0402411665731912);
    probs.put("6", 0.0296040891245602);
    probs.put("7", 0.0764543924947739);
    probs.put("8", 0.086581604038138);
    probs.put("9", 0.0398269005251619);
    probs.put("X", 0.0422296436037322);
    probs.put("Y", 0.056225462703309);

    try
      {
      Probability p = new Probability(probs);
      return p;
      }
    catch (ProbabilityException e)
      {
      log.error(e);
      }
    return null;
    }

  public static Map<Object, Probability> tempGetGainLossProb()
    {
    Map<Object, Probability> prob = new HashMap<Object, Probability>();

    Object[] gl = new Object[]{"gain", "loss"};

    try
      {
      prob.put("1", new Probability(gl, new double[]{0.4789, 0.5211}));
      prob.put("10", new Probability(gl, new double[]{0.3449, 0.6551}));
      prob.put("11", new Probability(gl, new double[]{0.4421, 0.5579}));
      prob.put("12", new Probability(gl, new double[]{0.5844, 0.4156}));
      prob.put("13", new Probability(gl, new double[]{0.3427, 0.6573}));
      prob.put("14", new Probability(gl, new double[]{0.2961, 0.7039}));
      prob.put("15", new Probability(gl, new double[]{0.4198, 0.5802}));
      prob.put("16", new Probability(gl, new double[]{0.2918, 0.7082}));
      prob.put("17", new Probability(gl, new double[]{0.3401, 0.6599}));
      prob.put("18", new Probability(gl, new double[]{0.4909, 0.5091}));
      prob.put("19", new Probability(gl, new double[]{0.6237, 0.3763}));
      prob.put("2", new Probability(gl, new double[]{0.5219, 0.4781}));
      prob.put("20", new Probability(gl, new double[]{0.5775, 0.4225}));
      prob.put("21", new Probability(gl, new double[]{0.5758, 0.4242}));
      prob.put("22", new Probability(gl, new double[]{0.3756, 0.6244}));
      prob.put("3", new Probability(gl, new double[]{0.4906, 0.5094}));
      prob.put("4", new Probability(gl, new double[]{0.2492, 0.7508}));
      prob.put("5", new Probability(gl, new double[]{0.4278, 0.5722}));
      prob.put("6", new Probability(gl, new double[]{0.3973, 0.6027}));
      prob.put("7", new Probability(gl, new double[]{0.6348, 0.3652}));
      prob.put("8", new Probability(gl, new double[]{0.7847, 0.2153}));
      prob.put("9", new Probability(gl, new double[]{0.3626, 0.6374}));
      prob.put("X", new Probability(gl, new double[]{0.4346, 0.5654}));
      prob.put("Y", new Probability(gl, new double[]{0.1117, 0.8883}));

      return prob;
      }
    catch (ProbabilityException e)
      {
      log.error(e);
      }

    return null;
    }


  public static Map<Object, Probability> tempCentromereProb()
    {
    Map<Object, Probability> prob = new HashMap<Object, Probability>();


    try
      {
      prob.put("21", new Probability(new Object[]{"p11", "p12", "q11"}, new double[]{0.0382, 0.0035, 0.0292}));
      prob.put("17", new Probability(new Object[]{"p11", "p12", "q11", "q12"}, new double[]{0.01465, 0.00366, 0.00749, 0.00509}));


      return prob;
      }
    catch (ProbabilityException e)
      {
      e.printStackTrace();
      }


    return null;
    }


  }
