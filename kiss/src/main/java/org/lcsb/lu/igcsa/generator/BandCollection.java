/**
 * org.lcsb.lu.igcsa.generator
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/*
Convinience class
 */
public class BandCollection<E> extends ArrayList<Band>
  {
  static Logger log = Logger.getLogger(BandCollection.class.getName());

  public BandCollection(Band[] bands)
    {
    for (Band b : bands)
      this.add(b);
    }

  public BandCollection(Collection<Band> bands)
    {
    super(bands);
    }

  public boolean hasCentromere()
    {
    Iterator<Band> bi = this.iterator();
    while (bi.hasNext())
      if (bi.next().isCentromere())
        return true;
    return false;
    }

  public List<Band> getCentromeres()
    {
    List<Band> cents = new ArrayList<Band>();
    Iterator<Band> bi = this.iterator();
    while (bi.hasNext())
      {
      Band b = bi.next();
      if (b.isCentromere())
        cents.add(b);
      }
    return cents;
    }

  // p
  public List<Band> getShortArm()
    {
    return this.getArm("p");
    }

  // q
  public List<Band> getLongArm()
    {
    return this.getArm("q");
    }

  private List<Band> getArm(String pq)
    {
    List<Band> arm = new ArrayList<Band>();
    Iterator<Band> bi = this.iterator();
    while (bi.hasNext())
      {
      Band b = bi.next();
      if (b.getBandName().matches(pq + "\\d+"))
        arm.add(b);
      }
    return arm;
    }
  }
