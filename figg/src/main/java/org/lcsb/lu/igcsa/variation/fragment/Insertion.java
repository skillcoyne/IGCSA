package org.lcsb.lu.igcsa.variation.fragment;

import org.lcsb.lu.igcsa.genome.DNASequence;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.LinkedHashMap;
import java.util.Random;

import static org.lcsb.lu.igcsa.genome.Nucleotides.*;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Insertion extends Variation
  {
  static Logger log = Logger.getLogger(Insertion.class.getName());


  public DNASequence mutateSequence(String sequence)
    {
    int count = this.fragment.getCount();
    lastMutations = new LinkedHashMap<Location, DNASequence>();

    log.debug(sequence.length() + " expected count " + count);

    // TODO what is the general probability of occurrence for each nucleotide anyhow??
    char[] validNucleotides = {A.value(), C.value(), T.value(), G.value()};

    int totalIns = 0;
    while (totalIns < count && sequence.length() > 1)
      {
      int nIndex = siteSelector.nextInt(sequence.length());
      int size = (Integer) this.sizeVariation.roll();

      log.debug("Site selected " + nIndex + " insertion size " + size);

      StringBuffer buf = new StringBuffer();
      for (int i=1; i<=size; i++)
        {
        char n = validNucleotides[ new Random().nextInt(3) ];
        buf.append(String.valueOf(n));
        }

      String newSequence =  sequence.substring(0, nIndex);
      newSequence = newSequence + buf.toString() + sequence.substring(nIndex, sequence.length());

      lastMutations.put(new Location(nIndex, nIndex+buf.length()), new DNASequence(buf.toString()));

      sequence = newSequence;
      ++totalIns;
      }

    return new DNASequence(sequence);
    }
  }
