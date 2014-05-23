package org.lcsb.lu.igcsa.karyotype.aberrations.single;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.genome.Band;
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
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Deletion extends SingleChromosomeAberration
  {
  static Logger log = Logger.getLogger(Deletion.class.getName());

  private String name = AberrationTypes.getNameFor(this.getClass().getName());


  @Override
  public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
    {
    List<Mutation> mutations = new ArrayList<Mutation>();

    log.info("Apply aberration to " + getFragments());

    TreeSet<Location> locations = new TreeSet<Location>();
    for (Band b: getFragments())
      locations.add(b.getLocation());

    FASTAReader reader = derivativeChromosome.getChromosomes().iterator().next().getFASTAReader();

    try
      {
      int lastEndLoc = 0;
      if (locations.first().getStart() > 0)
        {
        reader.reset(); // make sure we start from 0
        reader.streamToWriter(locations.first().getStart(), writer);

        lastEndLoc = locations.first().getEnd();

        mutations.add(new Mutation(derivativeChromosome.getChromosomes().iterator().next().getName(), locations.first().getStart(),
                                   locations.first().getEnd(), name));

        locations.remove(locations.first());
        }
      log.info("Start: " + lastEndLoc);
      // snip out each location
      for (Location loc : locations)
        {
        log.info(loc.getStart() + " " + loc.getEnd());

        mutations.add(new Mutation(derivativeChromosome.getChromosomes().iterator().next().getName(), loc.getStart(),
                                            loc.getEnd(), name));

        reader.streamToWriter(lastEndLoc, loc.getStart(), writer);
        lastEndLoc = loc.getEnd();
        }

      this.writeRemainder(reader, lastEndLoc, writer);

      writer.flush();
      writer.close();

      writeMutations(mutationWriter, mutations);
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }

    }
  }
