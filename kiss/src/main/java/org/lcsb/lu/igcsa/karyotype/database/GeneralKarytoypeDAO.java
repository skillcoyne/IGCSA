/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.database;

import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

public interface GeneralKarytoypeDAO
  {
  public Probability getProbabilityClass(String type) throws ProbabilityException;

  public Probability getChromosomeInstability() throws ProbabilityException;

  public Probability getBandProbabilities(String chr) throws ProbabilityException;

  public Probability getOverallBandProbabilities() throws ProbabilityException;


  /*
  These will return the raw probabilities from the database, rather than the probability object
   */
//  public Map<Object, Double> getProbabilityClass(String type) throws ProbabilityException;
//
//  public Map<Object, Double> getChromosomeInstability(boolean raw) throws ProbabilityException;
//
//  public Map<Object, Double> getBandProbabilities(String chr, boolean raw) throws ProbabilityException;
//
//  public Map<Object, Double> getOverallBandProbabilities(boolean raw) throws ProbabilityException;

  }
