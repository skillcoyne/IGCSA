package org.lcsb.lu.igcsa.database;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface FragmentDAO
  {

  // TODO These will need to use the correct table based on GC content...
  public int insertFragment(Fragment fragment);

  public Fragment getByRow(int rowNum);

  }
