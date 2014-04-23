package org.lcsb.lu.igcsa.karyotype.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class SequenceAberration
  {
  static Logger log = Logger.getLogger(SequenceAberration.class.getName());

  public abstract void addFragment(Band band, Chromosome chromosome);

  public abstract Collection<Band> getFragments();

  protected void writeRemainder(FASTAReader reader, int startLocation, FASTAWriter writer) throws IOException
    {
    log.info("Write file remainder");
    // write the remainder of the file
    int window = 5000;
    String seq = reader.readSequenceFromLocation(startLocation, window);
    if (seq != null)
      {
      writer.write(seq);
      while ((seq = reader.readSequence(window)) != null)
        {
        writer.write(seq);
        if (seq.length() < window)
          break;
        }
      }
    }

  protected void writeMutations(MutationWriter writer, List<Mutation> mutations) throws IOException
    {
    if (writer != null)
      {
      writer.write(mutations.toArray(new Mutation[mutations.size()]));
      writer.close();
      }

    }


  public abstract void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter);
  }
