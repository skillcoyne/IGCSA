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
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;

import static org.junit.Assert.*;

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


    }

  @Test
  public void testGetFragments() throws Exception
    {

    }

  @Test
  public void testGetFragmentLocations() throws Exception
    {

    }

  @Test
  public void testGetLocationsForChromosome() throws Exception
    {

    }
  }
