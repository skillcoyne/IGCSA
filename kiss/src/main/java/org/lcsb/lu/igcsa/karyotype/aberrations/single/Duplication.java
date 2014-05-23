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


/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Duplication extends SingleChromosomeAberration
  {
  static Logger log = Logger.getLogger(Duplication.class.getName());

  private String name = AberrationTypes.getNameFor(this.getClass().getName());

  @Override
  public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
    {
    List<Mutation> mutations = new ArrayList<Mutation>();

    log.info("Apply aberration to " + getFragments());


    List<Band> bands = new ArrayList<Band>(getFragments());
    Location location = bands.get(0).getLocation();

    // duplication involves copying an entire segment (location) and tacking it on to the end of the given location
    // so read from the start to the end of the location, then tack that location on. This means that there can only be
    // on location in a given duplication
    FASTAReader reader = derivativeChromosome.getChromosomes().iterator().next().getFASTAReader();

    try
      {
      reader.reset(); // make sure it's at 0
      reader.streamToWriter(location.getEnd(), writer);

      // go back to location start and tack that on
      writer.write(reader.readSequenceFromLocation(location.getStart(), 1));
      reader.streamToWriter(location.getEnd(), writer);

      writer.flush();
      writer.close();

      mutations.add(new Mutation(derivativeChromosome.getChromosomes().iterator().next().getName(), location.getStart(),
          location.getEnd(), name));

      writeMutations(mutationWriter, mutations);
      }
    catch (IOException e)
      {
      log.error(e);
      e.printStackTrace();
      }

    }
  }
