package org.lcsb.lu.igcsa.karyotype.database.normal;

import java.util.Map;

/**
 * org.lcsb.lu.igcsa.database.normal
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface VariationDAO
  {

  public Variations getVariations();

  public Map<String, Class> getVariationsByChromosome(String chr);

  }
