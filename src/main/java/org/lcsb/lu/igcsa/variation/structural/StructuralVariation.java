package org.lcsb.lu.igcsa.variation.structural;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class StructuralVariation
  {
  static Logger log = Logger.getLogger(StructuralVariation.class.getName());

  protected LinkedHashMap<Location, DNASequence> lastMutations;
  protected Random siteSelector = new Random();
  protected String variationName;
  protected Location variationLocation;

  protected Location[] toFrom = new Location[2];

  protected DNASequence sequence = new DNASequence();

  public void addFragment(DNASequence seq)
    {
    sequence.merge(seq);
    }

  public void setVariationName(String name)
    {
    this.variationName = name;
    }

  public String getVariationName()
    {
    return this.variationName;
    }

  public Location getLocation()
    {
    return variationLocation;
    }

  public void setLocation(Location variationLocation)
    {
    this.variationLocation = variationLocation;
    }

  public void setLocation(int s, int e)
    {
    this.variationLocation = new Location(s, e);
    }

  public Map<Location, DNASequence> getLastMutations()
    {
    return this.lastMutations;
    }

  public DNASequence mutateSequence(DNASequence sequence)
    {
    return this.mutateSequence(sequence.getSequence());
    }

  public abstract DNASequence mutateSequence(String sequence);

  }
