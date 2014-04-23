/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.karyotype.database.Band;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class KaryotypeCandidateTest
  {
  static Logger log = Logger.getLogger(KaryotypeCandidateTest.class.getName());

  KaryotypeCandidate candidate;

  @Before
  public void setUp() throws Exception
    {
    candidate = new KaryotypeCandidate();

    candidate.addBreakpoint( new Band("T", "xyz"));
    candidate.gainChromosome("Z");
    }

  @Test
  public void testClone() throws Exception
    {
    KaryotypeCandidate cloned = candidate.clone();
    assertEquals(cloned, candidate);
    }

  @Test
  public void testAddBreakpoints() throws Exception
    {
    List<Band> bands = new ArrayList<Band>();
    bands.add(new Band("X", "q11"));
    bands.add(new Band("2", "p32"));

    candidate.addBreakpoints(bands);
    assertEquals(candidate.getBreakpoints().size(), 3);
    }

  @Test
  public void testAddBreakpoint() throws Exception
    {
    candidate.addBreakpoint(new Band("T", "xyz"));
    assertEquals(candidate.getBreakpoints().size(), 1);

    candidate.addBreakpoint(new Band("QQ", "xyz"));
    assertEquals(candidate.getBreakpoints().size(), 2);
    }

  @Test
  public void testRemoveBreakpoint() throws Exception
    {
    List<Band> bands = new ArrayList<Band>();
    bands.add(new Band("X", "q11"));
    bands.add(new Band("2", "p32"));

    candidate.addBreakpoints(bands);
    assertEquals(candidate.getBreakpoints().size(), 3);

    candidate.removeBreakpoint(new Band("T", "xyz"));
    }

  @Test
  public void testGainChromosome() throws Exception
    {
    candidate.gainChromosome("X");

    assertEquals(candidate.getAneuploidy("X").getGain(), 1);
    assertEquals(candidate.getAneuploidy("X").getCount(), 1);

    candidate.gainChromosome("X");
    assertEquals(candidate.getAneuploidy("X").getGain(), 2);
    assertEquals(candidate.getAneuploidy("X").getCount(), 2);
    }

  @Test
  public void testLoseChromosome() throws Exception
    {
    candidate.loseChromosome("X");

    assertEquals(candidate.getAneuploidy("X").getLoss(), 1);
    assertEquals(candidate.getAneuploidy("X").getCount(), -1);
    }

  @Test
  public void testHasBreakpoint() throws Exception
    {
    assertTrue(candidate.hasBreakpoint(new Band("T", "xyz")));
    }

  @Test
  public void testGetBreakpoints() throws Exception
    {
    List<Band> bands = new ArrayList<Band>();
    bands.add(new Band("X", "q11"));
    bands.add(new Band("2", "p32"));

    candidate.addBreakpoints(bands);
    assertEquals(candidate.getBreakpoints().size(), 3);
    }

  @Test
  public void testGetAneuploidies() throws Exception
    {
    candidate.gainChromosome("X");
    candidate.loseChromosome("2");

    assertEquals(candidate.getAneuploidies().size(), 2);
    }

  @Test
  public void testRemoveChromosome() throws Exception
    {
    candidate.gainChromosome("X");
    assertNotNull(candidate.getAneuploidy("X"));

    candidate.removeChromosome("X");
    assertEquals(candidate.getAneuploidy("X").getCount(), 0);
    }

  }
