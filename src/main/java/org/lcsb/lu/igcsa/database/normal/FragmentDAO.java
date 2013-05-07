package org.lcsb.lu.igcsa.database.normal;

import java.util.List;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface FragmentDAO
  {
  public Fragment getVariationFragment(String chr, int binId, String variation, int fragmentNum);

  public Integer getVariationCount(String chr, int binId, String variation, int fragmentNum);

  public List<Fragment> getFragment(String chr, int binId, int fragmentNum);

  }
