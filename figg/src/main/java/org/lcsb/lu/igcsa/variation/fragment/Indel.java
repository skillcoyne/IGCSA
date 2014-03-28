/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa.variation.fragment;

import org.lcsb.lu.igcsa.genome.DNASequence;

import org.apache.log4j.Logger
    ;

import java.util.Random;

/*
An insertion and a deletion, affecting 2 or more nucleotides

TODO need to change how this works.  Currently sequence_alterations are being considered indels as SA is a generic term

 */
public class Indel extends Variation
  {
  static Logger log = Logger.getLogger(Indel.class.getName());

  public DNASequence mutateSequence(String sequence, int count)
    {
    DNASequence newSequence;

    /* TODO
    So it seems that it can be an insertion/deletion of differing sizes, or it can look like a SNV (but 2 or more bp's in length)
    In the gvf files it appears to always get listed as a deletion or insertion with comments about it being a large indel or something
     That means I need to change this a bit, BUT it's not even in my database as a variation currently so it can wait.
     */

//    int site = siteSelector.nextInt(sequence.length());
//
//    int delSize = siteSelector.nextInt(3)


    int indel = new Random().nextInt(2);
    if (indel == 0)
      {
      this.setVariationName("insertion");
      Insertion ins = new Insertion();
      ins.setSizeVariation(this.sizeVariation);
      newSequence = ins.mutateSequence(sequence, count);
      lastMutations = ins.lastMutations;
      }
    else
      {
      this.setVariationName("deletion");
      Deletion del = new Deletion();
      del.setSizeVariation(this.sizeVariation);
      newSequence = del.mutateSequence(sequence, count);
      lastMutations = del.lastMutations;
      }

    return newSequence;
    }

  }
