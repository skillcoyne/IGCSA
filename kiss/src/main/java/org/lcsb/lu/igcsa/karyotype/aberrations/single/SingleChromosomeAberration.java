package org.lcsb.lu.igcsa.karyotype.aberrations.single;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.karyotype.aberrations.SequenceAberration;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.*;


/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public abstract class SingleChromosomeAberration extends SequenceAberration
  {
  static Logger log = Logger.getLogger(SingleChromosomeAberration.class.getName());

  protected String chromosome;

  protected Set<Location> locations = new TreeSet<Location>();

  private List<Band> bands = new ArrayList<Band>();


  public void addFragment(Band band)
    {
    this.addFragment(band, null);
    }

  /*
  In a SingleChromosomeAberration you can only have a list of locations for a single chromosome.  So it's just a
   */
  @Override
  public void addFragment(Band band, Chromosome chr)
    {
    log.info("Adding band " + band);
    if (bands.size() <= 0)
      chromosome = band.getChromosomeName();
    else
      {
      if (!chromosome.equals(band.getChromosomeName()))
        {
        log.error(this.getClass().getSimpleName() + " expects fragments for chromosome " + chromosome + ". Failed to add " + band);
        return;
        }
      }

    if (checkOverlap(locations, band.getLocation()))
      log.error(band.getChromosomeName() + " " + band.getLocation() + " overlaps other locations. Removing from " +
                "aberrations.");
    else
      {
      locations.add(band.getLocation());
      bands.add(band);
      }
    }

  protected boolean checkOverlap(Collection<Location> locations, Location newLocation)
    {
    if (locations == null || newLocation == null)
      throw new IllegalArgumentException("Locations cannot be undefined (null)");

    List<Location> sortedLocations = new ArrayList<Location>(locations); // should already be sorted but
    Collections.reverse(sortedLocations);

    return (sortedLocations.size() > 0) ? sortedLocations.get(0).overlapsLocation(newLocation) : false;
    }

  @Override
  public Collection<Band> getFragments()
    {
    return bands;
    }

  protected void checkOverlaps(Collection<Location> locations) throws Exception
    {
    List<Location> sortedLocations = new ArrayList<Location>(locations);
    Collections.sort(sortedLocations);
    Location lastLoc = null;

    Iterator<Location> locI = sortedLocations.iterator();
    while (locI.hasNext())
      {
      Location loc = locI.next();
      if (lastLoc != null && loc.overlapsLocation(lastLoc))
        {
        locI.remove();
        throw new Exception("Locations overlap for " + this.getClass().getName() + " removing.");
        }
      lastLoc = loc;
      }
    }

  //TODO this is fairly slow, worth thinking about how it could be sped up
  protected void reverseWrite(Location loc, FASTAReader reader, FASTAWriter writer) throws IOException
    {
    log.info("Reverse " + loc);
    int window = 1000;

    int start = loc.getEnd() - 1 - window;
    int count = 0;
    while (true)
      {
      log.info("Seq " + start + " " + loc.getStart());
      boolean lastSeq = false;
      if (start <= loc.getStart())
        {
        window -= (loc.getStart() - start);
        start = loc.getStart();
        lastSeq = true;
        }
      StringBuffer seq = new StringBuffer(reader.readSequenceFromLocation(start, window));
      writer.write(seq.reverse().toString());
      log.debug(log + " " + seq.toString());
      count += seq.length();

      start -= window;

      if (count >= loc.getLength() || lastSeq)
        break;
      }
    }


  }
