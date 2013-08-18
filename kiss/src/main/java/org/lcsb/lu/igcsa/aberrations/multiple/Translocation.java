package org.lcsb.lu.igcsa.aberrations.multiple;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
// Translocations are special cases of an aberration. This may need to be a different class (not extending aberration) altogether.
public class Translocation extends DerivativeChromosomeAberration
  {
  static Logger log = Logger.getLogger(Translocation.class.getName());

  public void applyAberrations(DerivativeChromosome dchr, FASTAWriter writer, MutationWriter mutationWriter)
    {
    log.info(fragments);
    List<Mutation> mutations = new ArrayList<Mutation>();

    boolean first = true;
    int n = 1;
    try
      {
      for (Map.Entry<Band, Chromosome> entry : fragments.entrySet())
        {
        Chromosome chr = entry.getValue();
        Location fragmentLocation = entry.getKey().getLocation();

        // First chromosome, read from beginning to start location
        if (first && fragmentLocation.getStart() > 0)
          {
          chr.getFASTAReader().reset(); // make sure we start from 0
          chr.getFASTAReader().streamToWriter(fragmentLocation.getStart() - 1, writer);

          mutations.add(new Mutation(chr.getName(), 0, fragmentLocation.getStart(), "trans"));
          }

        mutations.add(new Mutation(chr.getName(), fragmentLocation.getStart(), fragmentLocation.getEnd(), "trans"));

        writer.write(chr.getFASTAReader().readSequenceFromLocation(fragmentLocation.getStart(), 1));
        chr.getFASTAReader().streamToWriter(fragmentLocation.getEnd(), writer);

        // Last chromosome, read from end location to the end TODO it's possible that this should not be the case for translocations
        // hard to say at the moment
        if (n == fragments.size())
          {
          writeRemainder(chr.getFASTAReader(), fragmentLocation.getEnd(), writer, mutationWriter);

          mutations.add(new Mutation(chr.getName(), fragmentLocation.getEnd(), -1, "trans"));
          }

        first = false;
        ++n;
        }

      writer.flush();
      writer.close();

      if (mutationWriter != null)
        {
        mutationWriter.write(mutations.toArray(new Mutation[mutations.size()]));
        mutationWriter.close();
        }
      }
    catch (IOException ioe)
      {
      log.error(ioe);
      }
    }



  }
