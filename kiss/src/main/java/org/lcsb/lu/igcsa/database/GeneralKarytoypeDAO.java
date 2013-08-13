/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

public interface GeneralKarytoypeDAO
  {

  public Probability getProbabilityClass(String type) throws ProbabilityException;

  public Probability getChromosomeInstability() throws ProbabilityException;

  public Probability getBandProbabilities(String chr) throws ProbabilityException;

  }
