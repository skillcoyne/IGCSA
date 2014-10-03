/**
 * org.lcsb.lu.igcsa.generator
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.generator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.*;


/**
 * Utility class to contain a set of bands with an aberration.
 */
public class Aberration
  {
  static Logger log = Logger.getLogger(Aberration.class.getName());

  private List<Band> bands;
  private AberrationTypes aberration;

  public Aberration(Band[] bands, AberrationTypes aberration)
    {
    this.bands = new ArrayList<Band>();
    for (Band b: bands)
      this.bands.add(b);

    this.aberration = aberration;
    }

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


  public String getDescription()
    {
    String desc = "";
    for (Band b: getBands())
      desc = desc + b.getChromosomeName() + b.getBandName() + "(" + b.getLocation().getStart() + "-" + b.getLocation().getEnd() +");";

    return getAberration().getCytogeneticDesignation() + " " + desc.substring(0, desc.lastIndexOf(";"));
    }

  public String getFASTAName()
    {
    String fastaName = "der" + getBands().get(0).getFullName() + "-" + getBands().get(getBands().size()-1).getFullName();
    return fastaName;
    }


  public static Aberration parseAberration(String str)
    {
    String cyto = str.substring(0, str.indexOf("("));

    AberrationTypes type = AberrationTypes.getTypeByCyto(cyto);
    str = str.replaceFirst(cyto, "").replace("(","").replace(")", "");

    List<Band> bands = new ArrayList<Band>();

    for (String bd : str.split(","))
      {
      String chr = bd.substring(0, bd.indexOf(":"));
      bands.add(new Band(chr, "", new Location(chr, Long.parseLong(bd.substring(bd.indexOf(":") + 1, bd.indexOf("-"))), Long.parseLong(bd.substring(bd.indexOf("-") + 1, bd.length())))));
      }

    return new Aberration(bands, type);
    }



  @Override
  public String toString()
    {
    return aberration + ": " + bands.toString();
    }

  }
