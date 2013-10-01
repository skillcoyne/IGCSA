/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.bp;

import org.apache.log4j.Logger;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.SelectionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DESelectionStrategy implements SelectionStrategy<Object>
  {
  static Logger log = Logger.getLogger(DESelectionStrategy.class.getName());


  @Override
  public <S> List<S> select(List<EvaluatedCandidate<S>> population, boolean naturalFitnessScores, int selectionSize, Random rng)
    {
    // this will be an odd use of the engine I think. But I'll just shuffle the population and throw them all back up.  The CrossOver operator will alter it
    Collections.shuffle(population, new MersenneTwisterRNG());

    List<S> selected = new ArrayList<S>();
    for(EvaluatedCandidate<S> c: population)
      selected.add(c.getCandidate());

    return selected;
    }


  }
