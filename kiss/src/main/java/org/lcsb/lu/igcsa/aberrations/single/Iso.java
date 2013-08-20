package org.lcsb.lu.igcsa.aberrations.single;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * org.lcsb.lu.igcsa.aberrations.single
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Iso extends SingleChromosomeAberration
  {
  static Logger log = Logger.getLogger(Iso.class.getName());

  private String name = AberrationTypes.getNameFor(this.getClass().getName());

  @Override
  public void addFragment(Band band)
    {
    if (getFragments().size() <= 0 && band.isCentromere())
      super.addFragment(band);
    else
      log.error(this.getClass().getSimpleName() + " requires a single centromere. Not adding " + band);
    }

  @Override
  public void addFragment(Band band, Chromosome chr)
    {
    if (getFragments().size() <= 0 && band.isCentromere())
      super.addFragment(band, chr);
    else
      log.error(this.getClass().getSimpleName() + " requires a single centromere. Not adding " + band);
    }

  @Override
  public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
    {
    log.info("Apply aberration to " + getFragments());

    List<Mutation> mutations = new ArrayList<Mutation>();

    Band band = getFragments().iterator().next();
    try
      {
      Chromosome chr = derivativeChromosome.getChromosomes().iterator().next();
      //Location location = band.getLocation();
      Location location = new Location(0, band.getLocation().getEnd());

      if (band.whichArm().equals("p"))
        {
        // output everything from 0 to the end of the band then output in reverse
        chr.getFASTAReader().streamToWriter(0, location.getEnd()-1, writer);
        reverseWrite(location, chr.getFASTAReader(), writer);

        mutations.add(new Mutation(chr.getName(), 0, location.getEnd(), name));
        mutations.add(new Mutation(chr.getName(), location.getEnd(), 0, name));
        }
      else
        {
        // output everything in reverse from the end of the band to the end of the chromosome, then output forward from end of band to end of chromosome
        mutations.add(new Mutation(chr.getName(), location.getEnd(), 0, name));
        mutations.add(new Mutation(chr.getName(), 0, location.getEnd(), name));

        reverseWrite(location, chr.getFASTAReader(), writer);
        chr.getFASTAReader().streamToWriter(location.getStart(), location.getEnd()-1, writer);
        //writeRemainder(chr.getFASTAReader(), location.getEnd(), writer);
        }

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
