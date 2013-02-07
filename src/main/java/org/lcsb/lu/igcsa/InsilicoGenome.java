package org.lcsb.lu.igcsa;


import org.lcsb.lu.igcsa.utils.GenomeProperties;
import org.lcsb.lu.igcsa.utils.PropertiesUtil;

import java.io.IOException;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class InsilicoGenome
  {
  private static final String propertyFile = "genome.properties";
  protected GenomeProperties normalProperties;
  protected GenomeProperties cancerProperties;

  public static void main(String[] args) throws Exception
    {
    InsilicoGenome igAp = new InsilicoGenome(args);
    }

  public InsilicoGenome(String[] args) throws Exception
    {
    initProperties();
    }

  protected void initProperties() throws IOException
    {
    Properties generalProps = PropertiesUtil.readPropsFile(propertyFile);
    normalProperties = new GenomeProperties(generalProps, GenomeProperties.GenomeType.NORMAL);
    cancerProperties = new GenomeProperties(generalProps, GenomeProperties.GenomeType.CANCER);
    }

  }
