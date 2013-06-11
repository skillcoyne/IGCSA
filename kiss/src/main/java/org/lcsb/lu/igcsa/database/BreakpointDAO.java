package org.lcsb.lu.igcsa.database;

/**
 * org.lcsb.lu.igcsa.database.karyotype
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface BreakpointDAO
  {

  public Breakpoint[] getBreakpointsForClass(int bpclass);

  public Breakpoint[] getBreakpointsForChr(String chr);

  public Breakpoint getBreakpoint(String chr, String band, int bpclass);

  }
