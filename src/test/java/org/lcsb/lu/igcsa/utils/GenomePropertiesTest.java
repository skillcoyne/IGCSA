package org.lcsb.lu.igcsa.utils;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenomePropertiesTest
  {
  GenomeProperties props;
  @Before
  public void setUp() throws Exception
    {
    props = GenomeProperties.readPropertiesFile("test.properties", GenomeProperties.GenomeType.NORMAL);
    assertNotNull("Properties failed to load.", props);
    }

  @Test
  public void testVariations() throws Exception
    {
    assertNotNull(props.getVariationProperty("snp"));
    assertEquals(props.getVariationProperty("snp").getPropertySet("base").stringPropertyNames().size(), 16);
    }


  @Test
  public void testGetPropertySet() throws Exception
    {
    GenomeProperties baseProps = props.getPropertySet("variation");
    assertEquals(baseProps.stringPropertyNames().size(), 2);
    }
  }
