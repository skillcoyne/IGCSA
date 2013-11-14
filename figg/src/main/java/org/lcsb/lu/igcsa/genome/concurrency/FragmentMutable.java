package org.lcsb.lu.igcsa.genome.concurrency;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.*;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.tables
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FragmentMutable extends Mutable
  {
  static Logger log = Logger.getLogger(FragmentMutable.class.getName());

  private Chromosome chromosome;
  private int window;

  // Database connections
  private GCBinDAO binDAO;
  private FragmentDAO fragmentDAO;

  public FragmentMutable(Chromosome chr, int window)
    {
    this.chromosome = chr;
    this.window = window;
    }

  public void setConnections(GCBinDAO bin, FragmentDAO fragment)
    {
    this.binDAO = bin;
    this.fragmentDAO = fragment;
    }

  public Chromosome call()
    {
    final long startTime = System.currentTimeMillis();
    log.info("RUNNING mutations on chromosome " + chromosome.getName());
    if (binDAO == null || fragmentDAO == null)
      throw new RuntimeException("Missing database connections. Call FragmentMutable.setConnections() before running");

    int total = 0;
    log.debug(chromosome.getName());

    Location location = new Location(0, window); // FASTA locations are 0 based.
    try
      {
      chromosome.getFASTAReader().reset();
      }
    catch (IOException e)
      {
      throw new RuntimeException(e);
      }

    DNASequence currentSequenceFragment;
    while (true)
      {
      currentSequenceFragment = chromosome.readSequence(window);

      DNASequence mutatedSequence = mutateFragment(chromosome, currentSequenceFragment, location);

      total += mutatedSequence.getLength();
      try
        {
        writer.write(mutatedSequence.getSequence());
        }
      catch (IOException e)
        {
        log.error(e);
        }

      location = new Location(location.getEnd(), location.getEnd() + window);
      if (currentSequenceFragment.getLength() < window)
        break;
      }
    try
      {
      writer.flush();
      writer.close();

      mutationWriter.flush();
      mutationWriter.close();
      }
    catch (IOException e)
      {
      log.error(e);
      }
    final long elapsedTime = System.currentTimeMillis() - startTime;
    log.info("FINISHED mutating chromosome " + chromosome.getName() + " sequence length " + total + " time:" + elapsedTime / 1000);
    return chromosome;
    }

  // Mutates the sequence based on the information provided in the database
  private DNASequence mutateFragment(Chromosome chr, DNASequence sequence, Location location)
    {
    long start = System.currentTimeMillis();
    DNASequence mutatedSequence = sequence;
    Random randomFragment = new Random();

    // get the GC content in order to select the correct fragment bin
    // might speed things up to skip anything that is > 70% (maybe less even) unknown/gap
    int totalNucleotides = sequence.calculateNucleotides();
    if (totalNucleotides > (0.3 * sequence.getLength()))
      {
      Bin gcBin = this.binDAO.getBinByGC(chr.getName(), sequence.calculateGC()); // TODO some of these calls take > 20ms (not even most though)

      // get random fragment within the GC bin for each variation
      Map<String, Fragment> fragmentVarMap = fragmentDAO.getFragment(chr.getName(), gcBin.getBinId(), randomFragment.nextInt(gcBin.getSize()));

      // apply the variations to the sequence, each of them needs to apply to the same fragment
      // it is possible that one could override another (e.g. a deletion removes SNVs)
      for (Variation variation : chr.getVariantList())
        {
        Fragment fragment = fragmentVarMap.get(variation.getVariationName());
        log.debug("Chromosome " + chr.getName() + " MUTATING FRAGMENT " + fragment.toString());
        variation.setMutationFragment(fragment);
        mutatedSequence = variation.mutateSequence(mutatedSequence);

        if (mutationWriter != null)  // This isn't going to work when I add in location-based structural variation mutation
          writeVariations(chr, location, gcBin, variation, variation.getLastMutations());
        }

      long elapsed = System.currentTimeMillis() - start;
      log.debug(chr + " " + location.getStart() + "-" + location.getEnd() + " took " + elapsed);
      }

    //else
      //log.debug("Fragment was > 70% unknown/gap, skipping mutation.");

    return mutatedSequence;
    }

  private void writeVariations(Chromosome chr, Location fragment, Bin GC, Variation variation, Map<Location, DNASequence> lastMutations)
    {
    final Map<Location, DNASequence> mutations = lastMutations;

    if (mutations.size() > 0)
      {
      List<Mutation> mutationBuffer = new ArrayList<Mutation>();
      for (Map.Entry<Location, DNASequence> entry : mutations.entrySet())
        {
        Location loc = entry.getKey();
        DNASequence seq = entry.getValue();

        Mutation mutation = new Mutation();
        mutation.setChromosome(chr.getName());
        mutation.setFragment(fragment.getStart());
        mutation.setGCBin(GC.getBinId());
        mutation.setVariationType(variation.getVariationName());

        mutation.setStartLocation(loc.getStart());
        mutation.setEndLocation(loc.getEnd());
        mutation.setSequence(seq.getSequence());

        mutationBuffer.add(mutation);
        }
      try
        {
        mutationWriter.write(mutationBuffer.toArray(new Mutation[mutationBuffer.size()]));
        }
      catch (IOException e)
        {
        log.error(e);
        }

      }

    }


  }
