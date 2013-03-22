package org.lcsb.lu.igcsa.database.insilico;

import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.database.insilico
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface GenomeDAO
  {
  public int insertGenome(int name, String location);

  public Genome getGenomeByID(int id);

  public Genome getGenomeByName(int name);

  public Genome getGenomeByLocation(String location);
  }
