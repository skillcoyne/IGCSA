package org.lcsb.lu.igcsa.database.normal;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface FragmentVariationDAO
  {

  public Fragment getFragment(String chr, int binId, int fragmentNum);

  }
