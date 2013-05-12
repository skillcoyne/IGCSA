package org.lcsb.lu.igcsa.variation.fragment;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.LinkedHashMap;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Substitution extends Variation
  {
  static Logger log = Logger.getLogger(Substitution.class.getName());

  public DNASequence mutateSequence(String sequence)
    {
    int count = this.fragment.getCount();
    int size = (Integer) this.sizeVariation.roll();

    lastMutations = new LinkedHashMap<Location, DNASequence>();

    log.debug(sequence.length() + " expected count " + count);

    return new DNASequence(sequence);
    }
  }
