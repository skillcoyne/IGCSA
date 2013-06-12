package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class Aberration
  {
  protected List<ChromosomeFragment> chromosomeFragments = new ArrayList<ChromosomeFragment>();


  public void addFragment(ChromosomeFragment fragment)
    {
    chromosomeFragments.add(fragment);
    }

  public ChromosomeFragment[] getFragments()
    {
    return chromosomeFragments.toArray(new ChromosomeFragment[chromosomeFragments.size()]);
    }

  public abstract void applyAberrations();

  }
