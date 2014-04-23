/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;
import org.lcsb.lu.igcsa.karyotype.generator.Aneuploidy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MinimalKaryotype
  {
  static Logger log = Logger.getLogger(MinimalKaryotype.class.getName());

  private List<Aberration> aberrations;
  private List<Aneuploidy> aneuploidies;

  public MinimalKaryotype()
    {
    aberrations = new ArrayList<Aberration>();
    aneuploidies = new ArrayList<Aneuploidy>();
    }

  public MinimalKaryotype(Collection<Aberration> aberrations, Collection<Aneuploidy> aneuploidies)
    {
    this.aberrations = new ArrayList<Aberration>(aberrations);
    this.aneuploidies = new ArrayList<Aneuploidy>(aneuploidies);
    }

  public List<Aberration> getAberrations()
    {
    return aberrations;
    }

  public List<String> getLocationOnlyAberrations()
    {
    List<String> abrs = new ArrayList<String>();
    for (Aberration abr: aberrations)
      abrs.add(abr.getWithLocations());
    return abrs;
    }

  public void setAberrations(List<Aberration> aberrations)
    {
    this.aberrations = aberrations;
    }

  public List<Aneuploidy> getAneuploidies()
    {
    return aneuploidies;
    }

  public void setAneuploidies(List<Aneuploidy> aneuploidies)
    {
    this.aneuploidies = aneuploidies;
    }

  public void addAberration(Aberration abr)
    {
    this.aberrations.add(abr);
    }

  public void addAneuploidy(Aneuploidy anp)
    {
    this.aneuploidies.add(anp);
    }

  @Override
  public String toString()
    {
    return this.getClass().getSimpleName() + "[" + this.hashCode() + "]\n" + this.aneuploidies + "\n" + this.aberrations;
    }
  }
