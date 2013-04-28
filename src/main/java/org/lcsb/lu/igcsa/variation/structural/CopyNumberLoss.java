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

  @Override
  public DNASequence mutateSequence(String sequence)
    {
    log.info("Losing " + variationLocation.getLength() + " from " + sequence.length());

    DNASequence newSequence = new DNASequence("");
    if (variationLocation.getLength() > sequence.length())
      { // snp sequence out
      String chunkA = sequence.substring(0,variationLocation.getStart()); // get sequence before
      String chunkB = sequence.substring(variationLocation.getEnd(), sequence.length()); // get sequence after

      newSequence = new DNASequence(chunkA + chunkB);
      }

    log.debug("Original sequence length: " + sequence.length());
    log.debug("New sequence length: " + newSequence.getLength());

    return newSequence;
    }
  }
