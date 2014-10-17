/**
 * org.lcsb.lu.igcsa.population.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.population.watchmaker.kt.termination;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.population.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import java.util.*;

public class BreakpointCondition implements TerminationCondition
  {
  static Logger log = Logger.getLogger(BreakpointCondition.class.getName());

  private Collection<Band> breakpoints;
  private int maxZeros;

  public BreakpointCondition(Collection<Band> breakpoints, int zeroCount)
    {
    this.breakpoints = breakpoints;
    this.maxZeros = zeroCount;
    }

  @Override
  public boolean shouldTerminate(PopulationData<?> populationData)
    {
    Map<Band, Integer> possibleBreakpoints = new HashMap<Band, Integer>();
    for (Band band : this.breakpoints)
      possibleBreakpoints.put(band, 0);

    List<? extends EvaluatedCandidate<?>> population = populationData.getEvaluatedPopulation();

    for (EvaluatedCandidate<?> candidate : population)
      {
      KaryotypeCandidate kc = (KaryotypeCandidate) candidate.getCandidate();
      for (Band bp : kc.getBreakpoints())
        possibleBreakpoints.put(bp, possibleBreakpoints.get(bp) + 1);
      }


    if (populationData.getGenerationNumber() > 40)
      {
      int zeros = Collections.frequency(possibleBreakpoints.values(), 0);
      log.debug("---> Zero frequency: " + zeros);
      if (zeros <= this.maxZeros)
        return true;
      }

    return false;
    }
  }
