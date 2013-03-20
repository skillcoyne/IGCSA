package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.genome.DNASequence;

import org.apache.log4j.Logger
;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Translocation extends Variation
  {
  static Logger log = Logger.getLogger(Translocation.class.getName());

  //TODO  this may not work for translocations...
  public DNASequence mutateSequence(String sequence)
    {
    return new DNASequence(sequence);
    }
  }
