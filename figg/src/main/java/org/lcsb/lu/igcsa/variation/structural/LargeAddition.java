package org.lcsb.lu.igcsa.variation.structural;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;

/**
 * org.lcsb.lu.igcsa.variation.structural
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LargeAddition extends StructuralVariation
  {
  static Logger log = Logger.getLogger(LargeAddition.class.getName());

  public DNASequence mutateSequence(String sequence)
    {
    // TODO Get number of time's the sequence should be duplicated
    int count = 3;

    DNASequence newSequence = new DNASequence("");
    if (sequence.length() > variationLocation.getLength())
      { // snp sequence out
      String pre = sequence.substring(0,variationLocation.getStart()); // get sequence before
      String insertion = sequence.substring(variationLocation.getStart(), variationLocation.getEnd());
      String post = sequence.substring(variationLocation.getEnd(), sequence.length()); // get sequence after

      // put it together
      newSequence.merge(pre);
      for (int i=0; i<count; i++) newSequence.merge(insertion);
      newSequence.merge(post);
      }
    else
      for (int i=0; i<count; i++) newSequence.merge(sequence);

    return newSequence;
    }
  }
