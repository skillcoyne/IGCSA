package org.lcsb.lu.igcsa.variation.structural;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;


/**
 * org.lcsb.lu.igcsa.variation.structural
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class CopyNumberVariation extends StructuralVariation
  {
  static Logger log = Logger.getLogger(CopyNumberVariation.class.getName());

  //TODO implement
  @Override
  public DNASequence mutateSequence(String sequence)
    {
    log.debug("Not implemented");
    // I actually have no idea what this class should do that's different from Loss/Gain.  But in the data these all show up as separate
    // events.
    return new DNASequence(sequence);
    }
  }
