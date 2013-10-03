package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.uncommons.watchmaker.framework.EvolutionUtils;

import java.util.*;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class CandidateUtils
  {
  static Logger log = Logger.getLogger(CandidateUtils.class.getName());

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

  public static double breakpointDistance(KaryotypeCandidate c1, KaryotypeCandidate c2)
    {
    double score = 0.0;

    if (c1.getBreakpoints().size() > c2.getBreakpoints().size()) // c2 is smaller
      score = listCompare(c1.getBreakpoints(), c2.getBreakpoints());
    else if (c1.getBreakpoints().size() < c2.getBreakpoints().size()) // c1 is smaller
      score = listCompare(c2.getBreakpoints(), c1.getBreakpoints());
    else // same size
      score = listCompare(c2.getBreakpoints(), c1.getBreakpoints());

    return score;
    }


  private static double apListCompare(Collection<KaryotypeCandidate.Aneuploidy> longList, Collection<KaryotypeCandidate.Aneuploidy> shortList)
    {
    double score = longList.size() - shortList.size();

    List<KaryotypeCandidate.Aneuploidy> longList2 = new ArrayList<KaryotypeCandidate.Aneuploidy>(longList);

    for (KaryotypeCandidate.Aneuploidy ap: shortList)
      {
      if (longList.contains(ap) && longList2.get(longList2.indexOf(ap)).isGain() == ap.isGain() )
        score += 1;
      }

    return score;
    }


  private static double listCompare(Collection<Band> longList, Collection<Band> shortList)
    {
    double score = longList.size() - shortList.size();
    for (Band obj : shortList)
      {
      if (!longList.contains(obj))
        score += 1;
      }
    return score;
    }


  }
