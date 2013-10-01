/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;

import java.util.*;

public class KaryotypeCandidate
  {
  static Logger log = Logger.getLogger(KaryotypeCandidate.class.getName());

  private Set<Band> breakpoints = new HashSet<Band>();
  private Map<String, Integer> aneuploidy = new HashMap<String, Integer>();


  public void addBreakpoint(Band band)
    {
    this.breakpoints.add(band);
    }

  public void gainChromosome(String chr)
    {
    alterPloidy(chr, 1);
    }

  public void loseChromosome(String chr)
    {
    alterPloidy(chr, -1);
    }


  public Set<Band> getBreakpoints()
    {
    return breakpoints;
    }

  public Map<String, Integer> getAneuploidy()
    {
    return aneuploidy;
    }


  private void alterPloidy(String chr, int n)
    {
    if (!aneuploidy.containsKey(chr))
      aneuploidy.put(chr, 0);

    aneuploidy.put(chr, aneuploidy.get(chr) + n);
    }

  }
