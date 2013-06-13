package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class Aberration
  {
  static Logger log = Logger.getLogger(Aberration.class.getName());
  //protected List<ChromosomeFragment> chromosomeFragments = new ArrayList<ChromosomeFragment>();

  protected Map<String, TreeSet<Location>> chrLocations = new HashMap<String, TreeSet<Location>>();

  /*
  T
   */
  public void addFragment(ChromosomeFragment fragment)
    {
    //chromosomeFragments.add(fragment);
    if (!chrLocations.containsKey(fragment.getChromosome()))
      chrLocations.put(fragment.getChromosome(), new TreeSet<Location>());

    TreeSet<Location> list = chrLocations.get(fragment.getChromosome());
    list.add(fragment.getBandLocation());
    chrLocations.put(fragment.getChromosome(), list);

    try
      {
      checkOverlaps();
      }
    catch (Exception e)
      {
      log.error(fragment.getChromosome() + " " + fragment.getBandLocation().toString() + " overlaps other locations. Removing from aberrations.");
      }
    }

//  public ChromosomeFragment[] getFragments()
//    {
//    return chromosomeFragments.toArray(new ChromosomeFragment[chromosomeFragments.size()]);
//    }

  public Map<String, TreeSet<Location>> getFragmentLocations()
    {
    return chrLocations;
    }


  protected void checkOverlaps() throws Exception
    {
    for (TreeSet<Location> locs: chrLocations.values())
      {
      Location lastLoc = null;
      Iterator<Location> locI = locs.iterator();
      while (locI.hasNext())
      //for (Location loc: locs)
        {
        Location loc = locI.next();
        if (lastLoc != null && loc.overlapsLocation(lastLoc))
          {
          locI.remove();
          throw new Exception("Locations overlap for " + this.getClass().getName() + " aberration cannot be applied");
          }
        lastLoc = loc;
        }
      }
    }


  public abstract void applyAberrations();

  }
