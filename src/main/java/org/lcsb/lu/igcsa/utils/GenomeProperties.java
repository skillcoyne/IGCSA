package org.lcsb.lu.igcsa.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenomeProperties extends Properties
  {
  public enum GenomeType
    {
      NORMAL("normal"), CANCER("cancer");

    private String name;

    private GenomeType(String s)
      { name = s; }

    public String getName()
      { return name; }
    }

  private GenomeType type;

  public GenomeProperties(Properties props, GenomeType var)
    {
    super(props); type = var;

    try { loadVariationProperties(); }
    catch (IOException e) { e.printStackTrace(); System.exit(-1); }
    }

  public Properties getPropertySet(String prefix)
    {
    Properties p = new Properties(); for (Object k : this.keySet())
      {
      String key = k.toString(); if (key.startsWith(prefix))
        {
        String newKey = key.replace(prefix + ".", ""); p.setProperty(newKey, this.getProperty(key));
        }
      }
    return p;
    }


  private void loadVariationProperties() throws IOException
    {
    String variationProperty = this.getProperty("variation." + type.getName());
    String[] variationList = variationProperty.split(";");

    for (String s : variationList)
      {
      String fileName = type.getName() + File.separator + s + ".properties"; try
        {
        Properties varProp = PropertiesUtil.readPropsFile(fileName); this.putAll(varProp);
        }
      catch (FileNotFoundException fne)
        {
        System.out.println(fne.getMessage());
        }

      }
    }


  }
