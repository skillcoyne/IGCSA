package org.lcsb.lu.igcsa.aberrations.single;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
public class Addition extends SingleChromosomeAberration
  {
  static Logger log = Logger.getLogger(Addition.class.getName());


  @Override
  public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
    {
    List<Band> bands = new ArrayList<Band>(getFragments());
    Location breakpoint = bands.get(0).getLocation();
    //Location breakpoint = getLocationsForChromosome(derivativeChromosome).first();

    FASTAReader reader = derivativeChromosome.getChromosomes().iterator().next().getFASTAReader();

    // an "add" tacks on unknown genomic content at the breakpoint.
    try
      {
      reader.reset(); // make sure we start from 0
      reader.streamToWriter(breakpoint.getEnd(), writer);

      int unkLength = new Random().nextInt(1000);
      StringBuffer newSeq = new StringBuffer(unkLength);
      for (int i = 0; i <= unkLength; i++)
        newSeq.append("N");

      // add some chunk of unknown nucleotides
      writer.write(newSeq.toString());
      writer.flush();

      if (mutationWriter != null)
        {
        mutationWriter.write(new Mutation(derivativeChromosome.getChromosomes().iterator().next().getName(), breakpoint.getEnd(),
                                          breakpoint.getEnd()+newSeq.length(), "add"));
        mutationWriter.close();
        }
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }
    }
  }
