/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.lcsb.lu.igcsa.watchmaker.kt.CandidateGraph;

import static org.junit.Assert.*;

public class CandidateGraphTest
  {
  static Logger log = Logger.getLogger(CandidateGraphTest.class.getName());

  @Test
  public void testGetInstance() throws Exception
    {
    CandidateGraph cg = CandidateGraph.getGraph();
    assertNotNull(cg);

    CandidateGraph cg1 = CandidateGraph.getGraph();
    assertSame(cg, cg1);
    }
  }
