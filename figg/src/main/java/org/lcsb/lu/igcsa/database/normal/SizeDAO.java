package org.lcsb.lu.igcsa.database.normal;

import org.lcsb.lu.igcsa.prob.Probability;
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

  public Map<String, Probability> getAll() throws ProbabilityException;

  public Probability getByVariation(String variation) throws ProbabilityException;

  }
