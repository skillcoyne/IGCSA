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

/*
A sequence alteration where the length of the change in the variant is the same as that of the reference.

TODO it's actually more complicated than this.  The reference sequence is 1 - n bp's long, and the replacement is 1 - n, but they don't necessarily match in length.  Either one can be longer than the other.  So...
*/
public class Substitution extends Variation
  {
  static Logger log = Logger.getLogger(Substitution.class.getName());

  @Override
  public DNASequence mutateSequence(String sequence, int count)
    {
    lastMutations = new LinkedHashMap<Location, DNASequence>();

    log.debug(sequence.length() + " expected count " + count);
    for (int i = 0; i < count; i++)
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
          //log.("Site: " + site);
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
