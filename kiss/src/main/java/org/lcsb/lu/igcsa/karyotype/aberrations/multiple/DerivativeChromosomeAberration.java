package org.lcsb.lu.igcsa.karyotype.aberrations.multiple;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.aberrations.SequenceAberration;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.genome.Chromosome;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa.aberrations.multiple
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class DerivativeChromosomeAberration extends SequenceAberration
  {
  static Logger log = Logger.getLogger(DerivativeChromosomeAberration.class.getName());

  // for a derivative fragments have to be in insertion order
  private Map<Band, Chromosome> fragments = new LinkedHashMap<Band, Chromosome>();

  @Override
  public void addFragment(Band band, Chromosome chr)
    {
    log.info("Adding fragment: " + band.toString());
    if (chr.getFASTAReader() == null)
      throw new IllegalArgumentException("Chromosome needs to include a FASTA file and reader.");

    fragments.put(band, chr);
    }

  @Override
  public Collection<Band> getFragments()
    {
    return fragments.keySet();
    }

  protected Map<Band, Chromosome> getFragmentMap()
    {
    return fragments;
    }


  }
