package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Nucleotides;

import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Insertion extends Variation
  {
  static Logger log = Logger.getLogger(Insertion.class.getName());


  public DNASequence mutateSequence(String sequence)
    {
    this.fragment.getInsertion();
    // TODO what is the general probability of occurrence for each nucleotide anyhow??
    char[] validNucleotides = Nucleotides.validDNA();
    return new DNASequence(sequence);
    }
  }
