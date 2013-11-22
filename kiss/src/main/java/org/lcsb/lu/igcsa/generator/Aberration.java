/**
 * org.lcsb.lu.igcsa.generator
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;

import java.util.*;


/**
 * Utility class to contain a set of bands with an aberration.
 */
public class Aberration
  {
  static Logger log = Logger.getLogger(Aberration.class.getName());

  private List<Band> bands;
  private AberrationTypes aberration;

  public Aberration(List<Band> bands, AberrationTypes aberration)
    {
    this.bands = bands;
    this.aberration = aberration;
    }

  public List<Band> getBands()
    {
    return bands;
    }

  public AberrationTypes getAberration()
    {
    return aberration;
    }

  public boolean areBandsIdentical()
    {
    if (bands.size() <= 1) throw new IllegalArgumentException("Can't test bands for uniqueness, only " + bands.size() + " band is in the aberration.");

    Set<Band> unique = new HashSet<Band>();
    for (Band b: bands)
      unique.add(b);
    if (unique.size() <= 1)
      return true;

    return false;
    }

  public boolean bandsOnSameArm()
    {
    String arm = bands.get(0).whichArm();
    for (Band b: bands)
      {
      if (!arm.equals(b.whichArm()))
        return false;
      }
    return true;
    }

  public boolean bandsIncludeCentromere()
    {
    for (Band b: bands)
      {
      if (b.isCentromere())
        return true;
      }
    return false;
    }


  public String getWithLocations()
    {
    List<String> strs = new ArrayList<String>();
    for(Band b: bands)
      strs.add(b.getChromosomeName() + ":" + b.getLocation().getStart() + "-" + b.getLocation().getEnd());

    return this.getAberration().getCytogeneticDesignation() + "(" + StringUtils.join(strs.iterator(), ",") + ")";
    }

  @Override
  public String toString()
    {
    return aberration + ": " + bands.toString();
    }

  }
