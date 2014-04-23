package org.lcsb.lu.igcsa.karyotype.database.normal;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.database.normal
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Variations
  {
  static Logger log = Logger.getLogger(Variations.class.getName());

  private Map<String, Integer> variationNametoID = new HashMap<String, Integer>();
  private Map<String, Class> variationNametoClass = new HashMap<String, Class>();


  public void addVariation(int id, String name, Class vclass)
    {
    variationNametoID.put(name, id);
    variationNametoClass.put(name, vclass);
    }

  public Class getClass(String name)
    {
    return variationNametoClass.get(name);
    }

  public Map<String, Integer> getVariationNametoID()
    {
    return variationNametoID;
    }

  public void setVariationNametoID(Map<String, Integer> variationNametoID)
    {
    this.variationNametoID = variationNametoID;
    }

  public Map<String, Class> getVariationNametoClass()
    {
    return variationNametoClass;
    }

  public void setVariationNametoClass(Map<String, Class> variationNametoClass)
    {
    this.variationNametoClass = variationNametoClass;
    }
  }
