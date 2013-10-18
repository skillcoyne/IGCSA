package org.lcsb.lu.igcsa.database;

import org.lcsb.lu.igcsa.genome.Location;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface ChromosomeBandDAO
  {

  public Band getBand(String chrBand);

  public Band getBandByChromosomeAndName(String chrName, String bandName);

  public Band[] getBands(String chrName);

  public Band getTerminus(String chr, String arm);

  public Location getLocation(Band band);

  }
