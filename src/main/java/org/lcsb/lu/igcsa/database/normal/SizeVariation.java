package org.lcsb.lu.igcsa.database.normal;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.prob.Frequency;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.util.Map;

/**
 * org.lcsb.lu.igcsa.database.insilico
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SizeVariation
  {
  static Logger log = Logger.getLogger(SizeVariation.class.getName());

  private String variation;
  private Frequency frequency;


  public String getVariation()
    {
    return variation;
    }

  public void setVariation(String variation)
    {
    this.variation = variation;
    }

  public Frequency getFrequency()
    {
    return frequency;
    }


  public void setFrequency(Map<Object, Double> probability)
    {
    try
      {
      this.frequency = new Frequency(probability, 10000); // TODO bad hardcoding...
      }
    catch (ProbabilityException e)
      {
      log.error(e);
      }
    }

  public void setFrequency(Frequency frequency)
    {
    this.frequency = frequency;
    }
  }
