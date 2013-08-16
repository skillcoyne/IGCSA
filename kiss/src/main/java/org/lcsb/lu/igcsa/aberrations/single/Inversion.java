package org.lcsb.lu.igcsa.aberrations.single;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Inversion extends SingleChromosomeAberration
  {
  static Logger log = Logger.getLogger(Inversion.class.getName());

  @Override
  public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
    {
    List<Mutation> mutations = new ArrayList<Mutation>();

    TreeSet<Location> locations = getLocationsForChromosome(derivativeChromosome);

    FASTAReader reader = derivativeChromosome.getChromosomes().iterator().next().getFASTAReader();

    try
      {
      if (locations.first().getStart() > 0)
        {
        reader.reset(); // make sure we start from 0
        reader.streamToWriter(locations.first().getStart()-1, writer);
        }

      Location lastLoc = locations.first();
      for (Location loc : locations)
        {
        mutations.add( new Mutation(derivativeChromosome.getChromosomes().iterator().next().getName(), loc.getStart(), loc.getEnd(),
                                    "inv") );

        int window = 100;
        // If there's a gap between last location and current we need to output it before moving on
        if (!lastLoc.equals(loc) && loc.getStart() > lastLoc.getEnd())
          reader.streamToWriter(lastLoc.getEnd(), loc.getStart(), writer);

        if (loc.getLength() < window)
          {
          StringBuffer seq = new StringBuffer(reader.readSequenceFromLocation(loc.getStart(), loc.getLength()));
          writer.write(seq.reverse().toString());
          }
        else
          {
          //Inversions could be so large that I need to read from the fasta file in reverse...
          int start = loc.getEnd() - window;
          int count = 0;
          while (true)
            {
            boolean lastSeq = false;
            if (start <= loc.getStart())
              {
              window -= (loc.getStart() - start);
              start = loc.getStart();
              lastSeq = true;
              }
            StringBuffer seq = new StringBuffer(reader.readSequenceFromLocation(start, window));
            writer.write(seq.reverse().toString());
            log.debug(log + " " + seq.toString());
            count += seq.length();

            start -= window;

            if (count >= loc.getLength() || lastSeq) break;
            }
          }
        lastLoc = loc;
        }

      writeRemainder(reader, lastLoc.getEnd(), writer, mutationWriter);

      writer.flush();
      writer.close();

      if (mutationWriter != null)
        {
        mutationWriter.write(mutations.toArray(new Mutation[mutations.size()]));
        mutationWriter.close();
        }

      }

    catch (IOException e)
      {
      e.printStackTrace();
      }


    }
  }
