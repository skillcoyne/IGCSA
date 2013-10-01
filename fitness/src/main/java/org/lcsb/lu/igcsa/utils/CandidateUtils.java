package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;

import java.util.ArrayList;
import java.util.List;


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
    for (KaryotypeCandidate candidate: karyotypeCandidates)
      bands.addAll(candidate.getBreakpoints());

    return bands;
    }
  }
