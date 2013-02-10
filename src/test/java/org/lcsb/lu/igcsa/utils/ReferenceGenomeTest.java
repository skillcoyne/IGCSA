package org.lcsb.lu.igcsa.utils;

import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.genome.Chromosome;

import static org.junit.Assert.*;

import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenomeTest
  {
  private GenomeProperties props;
  private ReferenceGenome rg;

  @Before
  public void setUp() throws Exception
    {
    props = new GenomeProperties(PropertiesUtil.readPropsFile("test.properties"), GenomeProperties.GenomeType.NORMAL);
    rg = new ReferenceGenome( props.getProperty("assembly", "test")  , props.getProperty("dir.assembly") );
    }

  @Test
  public void getChromosomeList() throws Exception
    {
    Chromosome[] chrArray = rg.getChromosomes().toArray( new Chromosome[rg.getChromosomes().size()] );
    assertEquals(rg.getChromosomes().size(), 1);
    assertEquals(chrArray[0].getName(), "19");
    assertTrue(chrArray[0].getFASTAFile().isFile());
    }


  @Test
  public void testGenome() throws Exception
    {

    }

  }
