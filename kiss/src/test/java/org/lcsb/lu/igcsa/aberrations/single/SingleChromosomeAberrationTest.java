/**
 * org.lcsb.lu.igcsa.aberrations.single
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.aberrations.single;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class SingleChromosomeAberrationTest
  {
  static Logger log = Logger.getLogger(SingleChromosomeAberrationTest.class.getName());

  private SingleChromosomeAberration abr;


  @Before
  public void setUp() throws Exception
    {
    abr = new SingleChromosomeAberration()
    {
    @Override
    public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
      {
      // not for this test
      }
    };

    assertNotNull(abr);
    }


  @Test
  public void testAddFragment() throws Exception
    {
    abr.addFragment(new Band("2", "q11", new Location(1,53)));
    assertEquals(abr.getFragments().size(), 1);

    abr.addFragment(new Band("2", "q11", new Location(10,73)));
    assertEquals(abr.getFragments().size(), 1);

    abr.addFragment(new Band("5", "q11", new Location(10,73)));
    assertEquals(abr.getFragments().size(), 1);

    abr.addFragment(new Band("2", "q23", new Location(99,112)));
    assertEquals(abr.getFragments().size(), 2);
    }


  }
