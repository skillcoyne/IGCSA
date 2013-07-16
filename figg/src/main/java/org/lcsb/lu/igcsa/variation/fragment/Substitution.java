/**
 * org.lcsb.lu.igcsa.variation.fragment
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa.variation.fragment;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.LinkedHashMap;
import java.util.Random;

import static org.lcsb.lu.igcsa.genome.Nucleotides.*;

public class Substitution extends Variation
  {
  /*
  A sequence alteration where the length of the change in the variant is the same as that of the reference.
   */
  static Logger log = Logger.getLogger(Substitution.class.getName());

  @Override
  public DNASequence mutateSequence(String sequence)
    {
    int count = this.fragment.getCount();
    lastMutations = new LinkedHashMap<Location, DNASequence>();

    log.debug(sequence.length() + " expected count " + count);
    for (int i = 0; i <= count; i++)
      {
      // TODO what is the general probability of occurrence for each nucleotide anyhow??
      char[] validNucleotides = {A.value(), C.value(), T.value(), G.value()};

      int size = (Integer) this.sizeVariation.roll();
      int site = siteSelector.nextInt(sequence.length());
      Location newLoc = new Location(site, site + size);
      // need to exclude previous sites
      for (Location loc: lastMutations.keySet())
        {
        while ( loc.overlapsLocation(newLoc) )
          {
          site = siteSelector.nextInt(sequence.length() - size);
          newLoc = new Location(site, site + size);
          }
        }
      StringBuffer buf = new StringBuffer();
      buf.append(sequence.substring(0, site));

      for (int n = 1; n <= size; n++)
        buf.append(validNucleotides[new Random().nextInt(3)]);

      if (site + size < sequence.length())  // +1 because
        buf.append(sequence.substring(site + size, sequence.length()));

      sequence = buf.toString();
      lastMutations.put(new Location(site, site + size), new DNASequence(sequence));
      }

    return new DNASequence(sequence);
    }
  }
