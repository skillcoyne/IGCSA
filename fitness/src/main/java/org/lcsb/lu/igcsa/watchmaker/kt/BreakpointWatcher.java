package org.lcsb.lu.igcsa.watchmaker.kt;

import org.lcsb.lu.igcsa.database.Band;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class BreakpointWatcher
  {
  private static BreakpointWatcher ourInstance = null;

  private Map<Band, Integer> breakpointCounts;

  public static BreakpointWatcher getInstance()
    {
    if (ourInstance == null)
      ourInstance = new BreakpointWatcher();

    return ourInstance;
    }

  private BreakpointWatcher()
    {
    breakpointCounts = new HashMap<Band, Integer>();
    }

  public void addAll(Collection<Band> bands)
    {
    for (Band band: bands)
      add(band);
    }

  protected void add(Band band)
    {
    if (!breakpointCounts.containsKey(band))
      breakpointCounts.put(band, 0);

    breakpointCounts.put(band, breakpointCounts.get(band)+1);
    }

  protected void removeAll(Collection<Band> bands)
    {
    for(Band band: bands)
      remove(band);
    }

  protected void remove(Band band)
    {
    breakpointCounts.put(band, breakpointCounts.get(band)-1);
    }

  public Map<Band, Integer> getBreakpointCounts()
    {
    return breakpointCounts;
    }
  }
