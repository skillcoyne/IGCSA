package org.lcsb.lu.igcsa.aberrations.single;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.SequenceAberration;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class SingleChromosomeAberration extends SequenceAberration
  {
  static Logger log = Logger.getLogger(SingleChromosomeAberration.class.getName());

  protected Map<String, TreeSet<Location>> chrLocations = new HashMap<String, TreeSet<Location>>();


  public void SingleChromosomeAberration()
    {

    }

  public void addFragment(Band band)
    {
    this.addFragment(band, null);
    }

  @Override
  public void addFragment(Band band, Chromosome chr)
    {
    log.info("Adding band " + band);
    if (!chrLocations.containsKey(band.getChromosomeName()))
      chrLocations.put(band.getChromosomeName(), new TreeSet<Location>());

    TreeSet<Location> list = chrLocations.get(band.getChromosomeName());
    list.add(band.getLocation());
    chrLocations.put(band.getChromosomeName(), list);

    try
      {
      checkOverlaps();
      }
    catch (Exception e)
      {
      log.error(band.getChromosomeName() + " " + band.getLocation() + " overlaps other locations. Removing from " +
                "aberrations.");
      }
    }

  public Map<String, TreeSet<Location>> getFragmentLocations()
    {
    return chrLocations;
    }

  public TreeSet<Location> getLocationsForChromosome(Chromosome chr)
    {
    return chrLocations.get(chr.getName());
    }

  protected void checkOverlaps() throws Exception
    {
    for (TreeSet<Location> locs : chrLocations.values())
      {
      Location lastLoc = null;
      Iterator<Location> locI = locs.iterator();
      while (locI.hasNext())
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


  }
