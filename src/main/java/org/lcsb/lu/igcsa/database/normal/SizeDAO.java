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
public interface SizeDAO
  {

  public Map<String, Frequency> getAll() throws ProbabilityException;

  public Frequency getByVariation(String variation) throws ProbabilityException;

  }
