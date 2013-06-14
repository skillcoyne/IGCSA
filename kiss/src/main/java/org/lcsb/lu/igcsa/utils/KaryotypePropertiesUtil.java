package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.aberrations.Aberration;
import org.lcsb.lu.igcsa.aberrations.Translocation;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.ChromosomeBandDAO;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;



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

  public static List<Aberration> getAberrationList(ChromosomeBandDAO bandDAO, Properties properties) throws Exception, IllegalAccessException, InstantiationException
    {
    List<Aberration> aberrationList = new ArrayList<Aberration>();
    for (String abrType: properties.stringPropertyNames())
      {
      Aberration AbrClass = (Aberration) Class.forName(abrType).newInstance();

      String aberrations = properties.getProperty(abrType);

      for (String abr: aberrations.split(","))
        {
        if (AbrClass.getClass().getName().equals(Translocation.class.getName()))
          { //14(q32)->11(q13)
          for (String breakpoint: abr.split("->"))
            {
            String[] chrBand = getChrBand(abr);
            Band band = bandDAO.getBandByChromosomeAndName(chrBand[0], chrBand[1]);
            AbrClass.addFragment(new ChromosomeFragment(band.getChromosomeName(), band.getBandName(), band.getLocation()));
            }
//          ChromosomeFragment lastFragment = AbrClass.getFragments()[AbrClass.getFragments().length-1];
//          // Terminal end  This takes the last breakpoint and finds terminal end of the chromosome, may turn out to be unnecessary
//          Band terminus = bandDAO.getTerminus(lastFragment.getChromosome(), lastFragment.getBand().substring(0,1));
//          AbrClass.addFragment(new ChromosomeFragment(terminus.getChromosomeName(), terminus.getBandName(), terminus.getLocation()));
          }
        else
          {
          String[] chrBand = getChrBand(abr);
          Band band = bandDAO.getBandByChromosomeAndName(chrBand[0], chrBand[1]);
          AbrClass.addFragment(new ChromosomeFragment(band.getChromosomeName(), band.getBandName(), band.getLocation()));
          }
        }
      aberrationList.add(AbrClass);
      }
    return aberrationList;
    }

  private static String[] getChrBand(String s)
    {
    String chr = s.substring(0, s.indexOf("("));
    String band = s.substring(s.indexOf("(")+1, s.indexOf(")"));

    return new String[]{chr, band};
    }


  }
