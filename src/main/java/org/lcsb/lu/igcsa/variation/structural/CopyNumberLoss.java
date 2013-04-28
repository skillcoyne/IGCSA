package org.lcsb.lu.igcsa.variation.structural;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;

/**
 * org.lcsb.lu.igcsa.variation.structural
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class CopyNumberLoss extends StructuralVariation
  {
  static Logger log = Logger.getLogger(CopyNumberLoss.class.getName());

  public DNASequence mutateSequence()
    {
    String sequenceToMutate = this.sequence.getSequence();

    String chunkA = sequenceToMutate.substring(0,variationLocation.getStart());
    String chunkB = sequenceToMutate.substring(variationLocation.getEnd(), sequenceToMutate.length());

    DNASequence newSequence = new DNASequence(chunkA + chunkB);

    log.debug("Original sequence length: " + sequenceToMutate.length());
    log.debug("New sequence length: " + newSequence.getLength());

    return newSequence;
    }
  }
