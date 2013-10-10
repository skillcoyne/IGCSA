/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;

import static org.junit.Assert.*;

public class CandidateUtilsTest
  {
  static Logger log = Logger.getLogger(CandidateUtilsTest.class.getName());

  Band[] bands;


  @Before
  public void setUp() throws Exception
    {
    bands = new Band[]{new Band("X", "q32"), new Band("7", "p11"), new Band("1", "q22"), new Band("3", "p22"), new Band("9", "q34")};
    }

  @Test
  public void testGetNCD() throws Exception
    {
    KaryotypeCandidate k1 = new KaryotypeCandidate();
    k1.addBreakpoint(bands[0]);
    k1.addBreakpoint(bands[bands.length - 1]);

    assertTrue("Identical objects", CandidateUtils.getNCD(k1, k1) < 0.08 );

    KaryotypeCandidate k2 = new KaryotypeCandidate();
    k2.addBreakpoint(bands[0]);
    k2.addBreakpoint(bands[bands.length - 1]);

    assertTrue("Same breakpoints, different objects", CandidateUtils.getNCD(k1, k2) < 0.08 );


    KaryotypeCandidate k3 = new KaryotypeCandidate();
    k3.addBreakpoint(bands[0]);
    k3.addBreakpoint(bands[1]);
    k3.addBreakpoint(bands[bands.length - 1]);

    assertTrue("Similar breakpoints", CandidateUtils.getNCD(k1, k3) > 0.1);
    }
  }
