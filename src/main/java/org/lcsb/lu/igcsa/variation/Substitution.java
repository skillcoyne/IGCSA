package org.lcsb.lu.igcsa.variation;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;

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
    int count = this.fragment.getSubstitution();
    log.info(sequence.length() + " expected count " + count);

    return new DNASequence(sequence);
    }
  }
