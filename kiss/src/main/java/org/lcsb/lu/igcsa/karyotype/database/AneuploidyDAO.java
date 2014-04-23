/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.database;

import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

public interface AneuploidyDAO
  {

  public Probability getGainLoss(String chromosome) throws ProbabilityException;

  public Probability getChromosomeProbabilities() throws ProbabilityException;



  }
