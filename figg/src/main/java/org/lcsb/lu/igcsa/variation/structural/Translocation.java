package org.lcsb.lu.igcsa.variation.structural;

import org.lcsb.lu.igcsa.genome.DNASequence;

import org.apache.log4j.Logger
;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Translocation extends StructuralVariation
  {
  static Logger log = Logger.getLogger(Translocation.class.getName());

  // TODO: implement
  public DNASequence mutateSequence(String sequence)
    {
    // not yet sure how to do a translocation.  It has to be able to move from one chromosome to another, or within a single chromosome.
    return this.sequence;
    }
  }
