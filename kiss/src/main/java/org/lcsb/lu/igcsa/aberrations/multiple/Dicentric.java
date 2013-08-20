package org.lcsb.lu.igcsa.aberrations.multiple;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa.aberrations.multiple
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Dicentric extends DerivativeChromosomeAberration
  {
  static Logger log = Logger.getLogger(Dicentric.class.getName());

  private String name = AberrationTypes.getNameFor(this.getClass().getName());

  @Override
  public void addFragment(Band band, Chromosome chr)
    {
    if (getFragments().size() <= 1)
      super.addFragment(band, chr);
    else
      log.warn(this.getClass().getSimpleName() + " expects only 2 bands. Failed to add " + band);
    }

  @Override
  public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
    {
    List<Mutation> mutations = new ArrayList<Mutation>();

    if (!hasCentromere())
      log.error(this.getClass().getSimpleName() + " requires at least one centromere.");
    else
      {
      log.info(getFragments());

      List<Band> bandList = sortBands();

      log.info(bandList);

      try
        {
        // First band, read from beginning to start location
        Band band = bandList.get(0);
        Chromosome chr = getFragmentMap().get(band);
        Location location = band.getLocation();

        chr.getFASTAReader().reset();
        chr.getFASTAReader().streamToWriter(0, location.getEnd() + 1, writer);

        mutations.add(new Mutation(chr.getName(), 0, location.getEnd(), name));


        // Second band, read from start location to end
        band = bandList.get(1);
        chr = getFragmentMap().get(band);
        location = band.getLocation();

        writeRemainder(chr.getFASTAReader(), location.getStart(), writer);


        mutations.add(new Mutation(chr.getName(), location.getStart(), -1, name));

        writer.flush();
        writer.close();

        writeMutations(mutationWriter, mutations);
        }
      catch (IOException ioe)
        {
        log.error(ioe);
        }
      }
    }

  // Needs to have at least one centromere
  private boolean hasCentromere()
    {
    for (Band b : getFragments())
      if (b.isCentromere())
        return true;

    return false;
    }

  private List<Band> sortBands()
    {
    List<Band> bandList = new ArrayList<Band>(getFragmentMap().keySet());

    // first band is p and second arm is q or both are p
    if ((bandList.get(0).whichArm().equals("p") && bandList.get(1).whichArm().equals("q")) || (bandList.get(0).whichArm().equals("p") && bandList.get(1).whichArm().equals("p")))
      {
      Collections.reverse(bandList);
      return bandList;
      }

    return bandList;
    }


  }
