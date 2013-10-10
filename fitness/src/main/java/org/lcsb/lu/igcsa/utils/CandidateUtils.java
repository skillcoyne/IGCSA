package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.zip.GZIPOutputStream;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

/**
 * Just a container for frequently used methods
 */
public class CandidateUtils
  {
  static Logger log = Logger.getLogger(CandidateUtils.class.getName());



  /*
  NCD = ( C(xy) - min{C(x), C(y)} ) / max{C(x), C(y)}
   */
  public static double getNCD(KaryotypeCandidate c1, KaryotypeCandidate c2)
    {
    try
      {
      double Cx = getCompressionSize( new KaryotypeCandidate[]{c1} );
      double Cy = getCompressionSize( new KaryotypeCandidate[]{c2} );
      double Cxy = getCompressionSize( new KaryotypeCandidate[]{c1, c2} );

      // Determine C(xy) - min{C(x),C(y)}
      // Determine max{C(x), C(y)}
      double maxC; double maxXY;
      if (Cx > Cy)
        {
        maxC = Cxy - Cy;
        maxXY = Cx;
        }
      else
        {
        maxC = Cxy - Cx;
        maxXY = Cy;
        }

      return round(maxC/maxXY, 5);
      }
    catch (IOException e)
      {
      log.error(e.getStackTrace());
      }

    return -1;
    }

  private static double getCompressionSize(KaryotypeCandidate[] candidates) throws IOException
    {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gz = new GZIPOutputStream(baos);
    ObjectOutputStream oos = new ObjectOutputStream(gz);

    for (KaryotypeCandidate candidate: candidates)
      {
      Collection<Band> breakpoints = candidate.getBreakpoints();
      String c = Arrays.toString(breakpoints.toArray(new Band[breakpoints.size()]));
      oos.write(c.getBytes());
      }

    oos.close();

    return baos.size();
    }

  public static double round(double d, int dec)
    {
    double places = Math.pow(10, dec);
    return (double) Math.round(d * places) / places;
    }



  public static List<Band> getBreakpoints(List<? extends KaryotypeCandidate> karyotypeCandidates)
    {
    List<Band> bands = new ArrayList<Band>();
    for (KaryotypeCandidate candidate : karyotypeCandidates)
      bands.addAll(candidate.getBreakpoints());

    return bands;
    }


  public static void testForDuplication(List<? extends KaryotypeCandidate> karyotypeCandidates)
    {
    Set<Integer> individuals = new HashSet<Integer>();

    for (KaryotypeCandidate kc : karyotypeCandidates)
      {
      if (individuals.contains(kc.hashCode()))
        throw new RuntimeException("Duplicate object found");

      individuals.add(kc.hashCode());
      }
    }


  public static double ploidyDistance(KaryotypeCandidate c1, KaryotypeCandidate c2)
    {
    double score = 0.0;

    if (c1.getAneuploidies().size() > c2.getAneuploidies().size())
      score = apListCompare(c1.getAneuploidies(), c2.getAneuploidies());
    else if (c1.getAneuploidies().size() < c2.getAneuploidies().size())
      score = apListCompare(c2.getAneuploidies(), c1.getAneuploidies());
    else
      score = apListCompare(c2.getAneuploidies(), c1.getAneuploidies());

    return score;
    }

  private static double apListCompare(Collection<KaryotypeCandidate.Aneuploidy> longList, Collection<KaryotypeCandidate.Aneuploidy> shortList)
    {
    double score = longList.size() - shortList.size();

    List<KaryotypeCandidate.Aneuploidy> longList2 = new ArrayList<KaryotypeCandidate.Aneuploidy>(longList);

    for (KaryotypeCandidate.Aneuploidy ap : shortList)
      {
      if (longList.contains(ap) && longList2.get(longList2.indexOf(ap)).isGain() == ap.isGain())
        score += 1;
      }

    return score;
    }


  }
