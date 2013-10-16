package org.lcsb.lu.igcsa.watchmaker.kt;

import org.lcsb.lu.igcsa.database.Band;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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

  private Collection<Band> expectedBreakpoints;

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

  public void setExpectedBreakpoints(Collection<Band> bands)
    {
    expectedBreakpoints = bands;
    reset();
    }

  protected void reset()
    {
    breakpointCounts.clear();

    for(Band b: expectedBreakpoints)
      breakpointCounts.put(b, 0);
    }



  protected void add(Band band)
    {
    if (expectedBreakpoints == null || !breakpointCounts.containsKey(band))
      throw new IllegalArgumentException("Expected breakpoints unset or tried to add a band that doesn't exist.");

    breakpointCounts.put(band, breakpointCounts.get(band)+1);
    }

  protected void remove(Band band)
    {
    if (expectedBreakpoints == null || !breakpointCounts.containsKey(band))
      throw new IllegalArgumentException("Expected breakpoints unset or tried to add a band that doesn't exist.");

    breakpointCounts.put(band, breakpointCounts.get(band)-1);
    }

  public Map<Band, Integer> getBreakpointCounts()
    {
    return breakpointCounts;
    }

  public void write(File outFile) throws IOException
    {
    if (!outFile.exists())
      outFile.createNewFile();

    FileWriter fileWriter = new FileWriter(outFile.getAbsoluteFile());

    fileWriter.write( this.write() );

    fileWriter.flush();
    fileWriter.close();
    }

  public String write() throws IOException
    {
    StringBuffer buff = new StringBuffer();
    buff.append("chr\tband\tcount\n");
    for (Map.Entry<Band, Integer> entry: breakpointCounts.entrySet())
      {
      Band band = entry.getKey();
      buff.append(band.getChromosomeName() + "\t" + band.getBandName() + "\t" + entry.getValue() + "\n");
      }

    return buff.toString();
    }




  }
