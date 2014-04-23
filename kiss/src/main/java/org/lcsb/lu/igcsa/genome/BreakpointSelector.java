package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.database.Breakpoint;
import org.lcsb.lu.igcsa.karyotype.database.BreakpointDAO;

import java.util.ArrayList;
import java.util.Random;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


/**
 * At the moment this class is meant to select the breakpoints that will applied to a tables.  I'm not entirely sure how.
 * The database provides probability estimates for each class of breakpoints but that's all.
 */
public class BreakpointSelector
  {
  static Logger log = Logger.getLogger(BreakpointSelector.class.getName());

  private BreakpointDAO breakpointDAO;

  public BreakpointSelector(BreakpointDAO breakpointDAO)
    {
    this.breakpointDAO = breakpointDAO;
    }

  public Breakpoint[] selectBreakpoints(int bpclass)
    {

    return new Breakpoint[0];
    }

  // For no particular reason right now select up to 5
  /*
  Here's a question...having selected bps I need to select their endpoint.  It -could- be that it's a single band
  but it could range to multiple bands.  Which means I'd need to pull from the same chromosome
   */
  private Breakpoint[] getCentromeres()
    {
    int totalCents = new Random().nextInt(5);
    Breakpoint[] bps = breakpointDAO.getBreakpointsForClass(1);
    Random bpSelector = new Random(bps.length);

    ArrayList<Breakpoint> selectedBps = new ArrayList<Breakpoint>();
    for (int i=0;i<=totalCents; i++)
      {
      int selected = bpSelector.nextInt();
      selectedBps.add(bps[selected]);
      }
    return selectedBps.toArray(new Breakpoint[selectedBps.size()]);
    }

  //private void

  }
