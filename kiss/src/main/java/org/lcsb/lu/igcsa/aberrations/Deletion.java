package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Deletion extends Aberration
  {
  static Logger log = Logger.getLogger(Deletion.class.getName());

  private void testLocations(TreeSet<Location> locations, long fileLength)
    {
    for (Location loc : locations)
      {
      if (loc.getStart() >= fileLength || loc.getEnd() >= fileLength)
        throw new IllegalArgumentException("Location " + loc.toString() + " is beyond the boundaries of the input file.");
      }
    }

  @Override
  public void applyAberrations(Chromosome chr, FASTAWriter writer, MutationWriter mutationWriter)
    {
    TreeSet<Location> locations = getLocationsForChromosome(chr);

    testLocations(locations, chr.getFASTA().length());

    try
      {
      int lastEndLoc = 0;
      if (locations.first().getStart() > 0)
        {
        chr.getFASTAReader().streamToWriter(0, locations.first().getStart(), writer);
        lastEndLoc = locations.first().getEnd();
        locations.remove(locations.first());
        }
      log.info("Start: " + lastEndLoc);
      // snip out each location
      for (Location loc : locations)
        {
        log.info(loc.getStart() + " " + loc.getEnd());
        int streamed = chr.getFASTAReader().streamToWriter(lastEndLoc, loc.getStart(), writer);
        log.info("streamed: " + streamed);
        lastEndLoc = loc.getEnd();
        }

      // write the remainder
      int start = lastEndLoc;
      int window = 1000;
      String seq = chr.getFASTAReader().readSequenceFromLocation(start, window);
      if (seq != null)
        {
        writer.write(seq);
        while (true)
          {
          seq = chr.getFASTAReader().readSequence(window);
          writer.write(seq);
          if (seq.length() < window) break;
          }
        }
      writer.flush();
      writer.close();

      }
    catch (IOException e)
      {
      e.printStackTrace();
      }

    }
  }
