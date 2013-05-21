package org.lcsb.lu.igcsa.variation.structural;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class StructuralVariation
  {
  static Logger log = Logger.getLogger(StructuralVariation.class.getName());

  /*
  An SV basically consists of one or more locations that are duplicated, deleted, inverted or merged
   */
  protected LinkedHashMap<Location, DNASequence> lastMutations;
  protected String variationName;
  protected List<Location> variationLocations = new LinkedList<Location>();

  protected DNASequence sequence = new DNASequence();

  public void addLocation(Location loc)
    {
    variationLocations.add(loc);
    }

  public void setLocations(Collection<Location> locs)
    {
    variationLocations = new LinkedList<Location>(locs);
    }

  public List<Location> getLocations()
    {
    return variationLocations;
    }

  public void setVariationName(String name)
    {
    this.variationName = name;
    }

  public String getVariationName()
    {
    return this.variationName;
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
