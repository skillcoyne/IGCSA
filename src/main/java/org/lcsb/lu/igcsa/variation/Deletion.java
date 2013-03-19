package org.lcsb.lu.igcsa.variation;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;



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
    this.fragment.getDeletion();
    return new DNASequence(sequence);
    }

  }
