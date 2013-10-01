/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.prob.Probability;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BandCandidateFactory extends AbstractCandidateFactory<Set<Band>>
  {
  static Logger log = Logger.getLogger(BandCandidateFactory.class.getName());

  private Probability bandProbability;
  private IntegerDistribution distribution;

  public BandCandidateFactory(Probability bandProbability, IntegerDistribution distribution)
    {
    this.distribution = distribution;
    this.bandProbability = bandProbability;
    }

  @Override
  public Set<Band> generateRandomCandidate(Random random)
    {
    int maxBands = distribution.sample();
    Set<Band> bands = new HashSet<Band>(maxBands);
    for (int i = 0; i < maxBands; i++)
      bands.add((Band) bandProbability.roll());

    return bands;
    }


  }
