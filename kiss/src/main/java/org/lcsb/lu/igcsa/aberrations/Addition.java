package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.Random;
import java.util.TreeSet;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

/*
"add" is the addition of some unknown genomic content to the given chromosome at the location and multiple "adds" in a single
chromosome are erroneous karyotype definitions. CyDAS treats "add" as a derivative chromosome. 2 can be in a karyotype with diploidy
but more than that causes an error as there are no further chromosomes

The aberration itself can only handle a single breakpoint.  So each time it's called with a chromosome,
the first location on the list is used then removed.
 */
public class Addition extends Aberration
  {
  static Logger log = Logger.getLogger(Addition.class.getName());

  @Override
  public void applyAberrations(Chromosome chr, FASTAWriter writer, MutationWriter mutationWriter)
    {
    Location breakpoint = getLocationsForChromosome(chr).first();

    FASTAReader reader = chr.getFASTAReader();

    // an "add" tacks on unknown genomic content at the breakpoint.
    try
      {
      reader.streamToWriter(0, breakpoint.getEnd(), writer);

      int unkLength = new Random().nextInt(1000);
      StringBuffer newSeq = new StringBuffer(unkLength);
      for (int i = 0; i <= unkLength; i++)
        newSeq.append("N");

      // add some chunk of unknown nucleotides
      writer.write(newSeq.toString());
      writer.flush();


      // TODO mutation writer needs to be specific for SVs too
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }


    }
  }
