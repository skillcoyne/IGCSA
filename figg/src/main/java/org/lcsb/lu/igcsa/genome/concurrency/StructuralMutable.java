package org.lcsb.lu.igcsa.genome.concurrency;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.variation.structural.StructuralVariation;

import java.io.IOException;
import java.util.*;


/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class StructuralMutable extends Mutable
  {
  static Logger log = Logger.getLogger(StructuralMutable.class.getName());

  private FASTAReader mutatedFASTA;


  public StructuralMutable(Chromosome chr)
    {
    this.chromosome = chr;
    this.mutatedFASTA = chromosome.getFASTAReader();
    }

  public void setMutatedFASTA(FASTAReader reader)
    {
    this.mutatedFASTA = reader;
    }

  // TODO Need to make sure that no locations are ever overlapping I think. You can't have a copy number gain and loss in the same
  // location for instance.  And translocations are a special case...
  // The filtering should probably happen when the database is queries though.
  private NavigableMap<Location, StructuralVariation> structuralVarLocations()
    {
    NavigableMap<Location, StructuralVariation> locationMap = new TreeMap<Location, StructuralVariation>();

    for (StructuralVariation sv : chromosome.getStructuralVariations())
      locationMap.put(sv.getLocation(), sv);

    return locationMap;
    }

  /*
  Run through all structural variations and mutate accordingly.
  TODO: This has to read in the mutated FASTA files and output new mutated files.
  Especially as there will be cases where sequence translocates from one chromosome to another.
  Which may mean that the variation class needs to know about writing?? Not sure about that.
   */
  // PROBLEM: Reading in big chunks of FASTA file means that java runs out of heap space.
  public Chromosome call()
    {
    log.info("Running structural mutations on " + chromosome.getName());
    NavigableMap<Location, StructuralVariation> locationMap = this.structuralVarLocations();

    int total = 0;
    Location firstLocation = locationMap.firstEntry().getKey();
    if (firstLocation.getStart() > 0)
      {
      try
        {
        String sequence = mutatedFASTA.readSequenceFromLocation(0, firstLocation.getStart());
        this.writer.write(sequence);
        total += sequence.length();
        }
      catch (IOException e)
        {
        log.error(e);
        }
      }

    // This should then occur in order
    for (Map.Entry<Location, StructuralVariation> entry : locationMap.entrySet())
      {
      Location loc = entry.getKey();
      StructuralVariation variation = entry.getValue();
      try
        {
        String sequence = mutatedFASTA.readSequenceFromLocation(loc.getStart(), loc.getLength());
        DNASequence mutatedSequence = variation.mutateSequence(sequence);
        this.writer.write(mutatedSequence.getSequence());
        writeVariation(chromosome, loc, variation);
        total += mutatedSequence.getLength();
        }
      catch (IOException e)
        {
        log.error(e);
        }
      }

    // output the rest of the sequence
    int lastLocation = locationMap.lastKey().getEnd();
    try
      {
      String seq = "";
      int window = 1000;
      while ((seq = mutatedFASTA.readSequenceFromLocation(lastLocation, window)) != null)
        {
        writer.write(seq);
        lastLocation += window;
        }
      mutationWriter.flush();
      mutationWriter.close();
      writer.flush();
      writer.close();
      }
    catch (IOException e)
      {
      log.error(e);
      }

    log.info("FINISHED mutating chromosome " + chromosome.getName() + " sequence length " + total);

    return chromosome;
    }


  private void writeVariation(Chromosome chr, Location location, StructuralVariation variation)
    {
    if (mutationWriter != null)
      {
      Mutation mutation = new Mutation();
      mutation.setChromosome(chr.getName());
      mutation.setVariationType(variation.getVariationName());
      mutation.setStartLocation(location.getStart());
      mutation.setEndLocation(location.getEnd());
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
