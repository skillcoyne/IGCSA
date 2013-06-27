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
  public static Object[] getTranslocations(ChromosomeBandDAO bandDAO, Properties properties, Map<String,
      Chromosome> chromosomeMap) throws Exception
    {
    List<Aberration> translocationList = new ArrayList<Aberration>();

    Set<String> chromosomes = new LinkedHashSet<String>();
    for (String abrType: properties.stringPropertyNames())
      {
      Aberration AbrClass = (Aberration) Class.forName(abrType).newInstance();

      String aberrations = properties.getProperty(abrType);

      for (String abr: aberrations.split(","))
        {
        // TRANSLOCATION
        if (AbrClass.getClass().getName().equals(Translocation.class.getName()))
          { //14(q32)->11(q13)
          Translocation trans = (Translocation) AbrClass;
          for (String breakpoint: abr.split("->"))
            {
            String[] chrBand = getChrBand(breakpoint);
            Band band = bandDAO.getBandByChromosomeAndName(chrBand[0], chrBand[1]);

            trans.addFragment(new ChromosomeFragment(band.getChromosomeName(), band.getBandName(), band.getLocation()),
                              chromosomeMap.get(band.getChromosomeName()));

            chromosomes.add(band.getChromosomeName());
            }
          translocationList.add(trans);
          }
        }
      }
    return (translocationList.size() > 0)? ( new Object[]{chromosomes, translocationList} ): null;
    }

  public static Map<String, List<Aberration>> getAberrationList(ChromosomeBandDAO bandDAO, Properties properties) throws Exception
    {
    Map<String, List<Aberration>> chrAbrMap = new HashMap<String, List<Aberration>>();

    for (String abrType: properties.stringPropertyNames())
      {
      Aberration AbrClass = (Aberration) Class.forName(abrType).newInstance();

      String aberrations = properties.getProperty(abrType);

      for (String abr: aberrations.split(","))
        {
        if (AbrClass.getClass().getName().equals(Translocation.class.getName())) continue;

        if (AbrClass.getClass().getName().equals(Duplication.class.getName()))
          { //12(q13:q22)
          String chr = abr.substring(0, abr.indexOf("("));
          String[] bands = abr.substring(abr.indexOf("(")+1, abr.indexOf(")")).split(":");

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
    String band = s.substring(s.indexOf("(")+1, s.indexOf(")"));
    return new String[]{chr, band};
    }


  }
