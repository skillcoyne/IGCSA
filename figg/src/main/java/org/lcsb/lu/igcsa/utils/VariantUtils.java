package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.database.normal.SNVProbabilityDAO;
import org.lcsb.lu.igcsa.karyotype.database.normal.SizeDAO;
import org.lcsb.lu.igcsa.karyotype.database.normal.VariationDAO;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.variation.fragment.SNV;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class VariantUtils
  {
  static Logger log = Logger.getLogger(VariantUtils.class.getName());

  private VariationDAO variationDAO;
  private SizeDAO sizeDAO;
  private SNVProbabilityDAO snvDAO;

  private Map<String, List<Variation>> variantListByChr = new HashMap<String, List<Variation>>();

  public VariantUtils(VariationDAO variationDAO, SizeDAO sizeDAO, SNVProbabilityDAO snvDAO)
    {
    this.variationDAO = variationDAO;
    this.sizeDAO = sizeDAO;
    this.snvDAO = snvDAO;
    }

  public List<Variation> getVariantList(String chr) throws IllegalAccessException, ProbabilityException, InstantiationException
    {
    //log.info("Getting variations for chromosome " + chr);
    if (!variantListByChr.containsKey(chr))
      {
      List<Variation> variantList = new ArrayList<Variation>();
      for(Map.Entry<String, Class> entry: variationDAO.getVariationsByChromosome(chr).entrySet())
        {
        if (entry.getKey().equals("SNV"))
          {
          SNV variant = (SNV) entry.getValue().newInstance();
          variant.setVariationName(entry.getKey());
          variant.setSnvFrequencies( snvDAO.getAll() );
          variantList.add(variant);
          }
        else
          {
          Variation variant = null;
          try
            {
            variant = (Variation) entry.getValue().newInstance();
            variant.setVariationName(entry.getKey());
            variant.setSizeVariation( sizeDAO.getByVariation(entry.getKey()) );
            variantList.add(variant);
            }
          catch (InstantiationException e)
            {
            log.warn(entry.getKey() + " is not currently implemented." + e);
            }
          }
        }
      variantListByChr.put(chr, variantList);
      }
    return variantListByChr.get(chr);
    }



  }
