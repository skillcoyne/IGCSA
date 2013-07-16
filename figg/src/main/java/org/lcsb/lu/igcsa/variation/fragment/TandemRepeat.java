package org.lcsb.lu.igcsa.variation.fragment;/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.LinkedHashMap;

/*
Two or more adjacent copies of a region (of length greater than 1).
 */
public class TandemRepeat extends Variation
  {
  static Logger log = Logger.getLogger(TandemRepeat.class.getName());

  @Override
  public DNASequence mutateSequence(String sequence)
    {
    lastMutations = new LinkedHashMap<Location, DNASequence>();

    int count = this.fragment.getCount();

    for (int i = 0; i < count; i++)
      {
      int size = (Integer) this.sizeVariation.roll();
      int siteStart = getStartSite(sequence.length(), size);

      Location newLoc = new Location(siteStart, siteStart + size);

      // need to exclude previous sites
      for (Location loc: lastMutations.keySet())
        {
        while ( loc.overlapsLocation(newLoc) )
          {
          siteStart = getStartSite(sequence.length(), size);
          newLoc = new Location(siteStart, siteStart + size);
          }
        }

      String copySeq = sequence.substring(siteStart - size, siteStart);

      StringBuffer buf = new StringBuffer();
      buf.append(sequence.substring(0, siteStart));
      buf.append(copySeq);
      buf.append(sequence.substring(siteStart, sequence.length()));

      sequence = buf.toString();

      lastMutations.put(new Location(siteStart, siteStart + size), new DNASequence(sequence));
      }

    return new DNASequence(sequence);
    }

  private int getStartSite(int seqLength, int size)
    {
    int start = siteSelector.nextInt(seqLength - size);
    while (start < size)
      start = siteSelector.nextInt(seqLength - size);

    return start;
    }

  }
