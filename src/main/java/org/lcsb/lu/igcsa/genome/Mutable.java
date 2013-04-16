package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.*;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Mutable implements Runnable
  {
  static Logger log = Logger.getLogger(Mutable.class.getName());

  private Chromosome chromosome;
  private int window;
  //private Collection<Variation> variations;

  private FASTAWriter writer;
  private MutationWriter mutationWriter;

  // Database connections
  private GCBinDAO binDAO;
  private FragmentVariationDAO variationDAO;
  private SizeDAO sizeDAO;

  public Mutable(Chromosome chr, int window)
    {
    this.chromosome = chr;
    this.window = window;
    }

  public void setWriters(FASTAWriter writer, MutationWriter mutationWriter)
    {
    this.writer = writer;
    this.mutationWriter = mutationWriter;
    }

  public void setConnections(GCBinDAO bin, FragmentVariationDAO fragment, SizeDAO size)
    {
    this.binDAO = bin;
    this.variationDAO = fragment;
    this.sizeDAO = size;
    }

  public void run()
    {
    log.info("Running mutations " + chromosome.getName());
    if (binDAO == null || variationDAO == null || sizeDAO == null)
      throw new RuntimeException("Missing database connections. Call Mutable.setConnections() before running");

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
      log.info("MUTATING " + chromosome.getName() + " at " + location.toString());
      DNASequence mutatedSequence = mutateSequenceAtLocation(chromosome, currentSequenceFragment, location);
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

    log.info("FINISHED mutating chromosome " + chromosome.getName() + " sequence length " + total);
    }

  // Mutates the sequence based on the information provided in the database
  private DNASequence mutateSequenceAtLocation(Chromosome chr, DNASequence sequence, Location location)
    {
    DNASequence mutatedSequence = sequence;
    Random randomFragment = new Random();

    // get the GC content in order to select the correct fragment bin
    int totalNucleotides = sequence.calculateNucleotides();
    if (totalNucleotides > 0)
      {
      Bin gcBin = this.binDAO.getBinByGC(chr.getName(), sequence.calculateGC());

      log.debug(gcBin.toString());
      // get random fragment within the GC bin
      Fragment fragment = this.variationDAO.getFragment(chr.getName(), gcBin.getBinId(), randomFragment.nextInt(gcBin.getSize()));
      if (totalNucleotides >= fragment.countSums())
        {
        log.debug("Chromosome " + chr.getName() + " MUTATING FRAGMENT " + fragment.toString());
        // apply the variations to the sequence, each of them needs to apply to the same fragment
        // it is possible that one could override another (e.g. a deletion removes SNVs)
        for (Variation variation : chr.getVariantList())
          {
          variation.setMutationFragment(fragment);
          mutatedSequence = variation.mutateSequence(mutatedSequence);

          if (mutationWriter != null)
            writeVariations(chr, location, variation, variation.getLastMutations());
          }
        }
      }
    return mutatedSequence;
    }

  private void writeVariations(Chromosome chr, Location fragment, Variation variation, Map<Location, DNASequence> lastMutations)
    {
    final Map<Location, DNASequence> mutations = lastMutations;

    if (mutations.size() > 0)
      {
      Mutation mutation = new Mutation();
      mutation.setChromosome(chr.getName());
      mutation.setFragment(fragment.getStart());
      mutation.setVariationType(variation.getVariationName());

      for (Map.Entry<Location, DNASequence> entry : mutations.entrySet())
        {
        Location loc = entry.getKey();
        DNASequence seq = entry.getValue();

        mutation.setStartLocation(loc.getStart());
        mutation.setEndLocation(loc.getEnd());
        mutation.setSequence(seq.getSequence());
        }
      try
        {
        mutationWriter.write(mutation);
        }
      catch (IOException e)
        {
        log.error(e);
        }
      }

    }

  }
