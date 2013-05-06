package org.lcsb.lu.igcsa.variation.fragment;

import org.lcsb.lu.igcsa.genome.DNASequence;

import org.apache.log4j.Logger
    ;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.LinkedHashMap;
import java.util.Random;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Indel extends Variation
  {
  static Logger log = Logger.getLogger(Indel.class.getName());

  public DNASequence mutateSequence(String sequence)
    {
    // indel & sequence_alteration are both used to denote an insertion OR deletion apparently.

    DNASequence newSequence;

    int indel = new Random().nextInt(2);
    if (indel == 0)
      {
      Insertion ins = new Insertion();
      ins.setSizeVariation(this.sizeVariation);
      ins.setMutationFragment(fragment);
      newSequence = ins.mutateSequence(sequence);
      lastMutations = ins.lastMutations;
      }
    else
      {
      Deletion del = new Deletion();
      del.setSizeVariation(this.sizeVariation);
      del.setMutationFragment(fragment);
      newSequence = del.mutateSequence(sequence);
      lastMutations = del.lastMutations;
      }

    return newSequence;
    }

  }
