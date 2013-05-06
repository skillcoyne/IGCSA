package org.lcsb.lu.igcsa.variation.fragment;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.LinkedHashMap;


/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Deletion extends Variation
  {
  static Logger log = Logger.getLogger(Deletion.class.getName());


  public DNASequence mutateSequence(String sequence)
    {
    int count = this.fragment.getCount();

    lastMutations = new LinkedHashMap<Location, DNASequence>();

    log.debug(sequence.length() + " expected count " + count);

    int totalDel = 0;
    while (totalDel < count)
      {
      if (sequence.length() <= 0 ) break;

      int nIndex = siteSelector.nextInt(sequence.length());
      int size = (Integer) this.sizeVariation.roll();

      if (size > sequence.length()-nIndex) size = sequence.length()-nIndex;

      log.debug("Site selected " + nIndex + " deletion size " + size + " sequence length " + sequence.length());

      String newSequence =  sequence.substring(0, nIndex);
      newSequence = newSequence + sequence.substring(nIndex + size, sequence.length());

      lastMutations.put(new Location(nIndex, nIndex+size), new DNASequence("-"));

      sequence = newSequence;
      ++totalDel;
      }

    return new DNASequence(sequence);
    }

  }
