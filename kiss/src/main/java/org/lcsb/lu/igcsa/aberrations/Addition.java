package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Chromosome;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Addition extends Aberration
  {
  static Logger log = Logger.getLogger(Addition.class.getName());

  @Override
  public void applyAberrations()
    {

    try
      {
      checkOverlaps();
      }
    catch (Exception e)
      {
      log.error(e);
      }
    log.info("Not yet implemented");
    }
  }
