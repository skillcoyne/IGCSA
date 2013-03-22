package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.insilico.GenomeDAO;
import org.lcsb.lu.igcsa.database.insilico.Mutation;
import org.lcsb.lu.igcsa.database.insilico.MutationDAO;
import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentVariationDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class MutableGenome implements Genome
  {
  static Logger log = Logger.getLogger(Genome.class.getName());

  protected String buildName;
  protected HashMap<String, Chromosome> chromosomes = new HashMap<String, Chromosome>();
  private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

  private GCBinDAO binDAO;
  private FragmentVariationDAO variationDAO;

  //  private GenomeDAO genomeDAO;
  //  private MutationDAO mutationDAO;

  private MutationWriter mutationWriter;

  private List<Variation> variantTypes;

  public MutableGenome(GCBinDAO gcBinDAO, FragmentVariationDAO variationDAO)
    {
    this.binDAO = gcBinDAO;
    this.variationDAO = variationDAO;
    }

  public void setMutationWriter(MutationWriter writer)
    {
    this.mutationWriter = writer;
    }


  protected MutableGenome(String buildName)
    {
    this.buildName = buildName;
    }

  //  public void setGenomeDAO(GenomeDAO genomeDAO)
  //    {
  //    this.genomeDAO = genomeDAO;
  //    }
  //
  //  public void setMutationDAO(MutationDAO mutationDAO)
  //    {
  //    this.mutationDAO = mutationDAO;
  //    }

  public void setBuildName(String buildName)
    {
    this.buildName = buildName;
    }

  public String getBuildName()
    {
    return this.buildName;
    }

  public List<Variation> getVariantTypes()
    {
    return variantTypes;
    }

  public void setVariantTypes(List<Variation> variantTypes)
    {
    this.variantTypes = variantTypes;
    }

  public void addChromosome(Chromosome chr)
    {
    this.chromosomes.put(chr.getName(), chr);
    log.debug("Added chromosome " + chr.getName() + ", have " + chromosomes.size() + " chromosomes");
    }

  public void addChromosomes(Chromosome[] chromosomes)
    {
    for (Chromosome chromosome : chromosomes)
      this.addChromosome(chromosome);
    }

  public boolean hasChromosome(String name)
    {
    return chromosomes.containsKey(name);
    }

  public Chromosome[] getChromosomes()
    {
    return chromosomes.values().toArray(new Chromosome[chromosomes.size()]);
    }


  /*
    Not recommended for general use unless you have very small chromosomes as this holds the entire
    genome in memory. Better use is to call #mutate(Chromosome chr, int window) and output the new
    chromosome.
   */
  public Genome mutate(int window, FASTAWriter writer)
    {
    //int genomeId = this.genomeDAO.insertGenome(id, writer.getFASTAFile().getAbsolutePath());

    Genome newGenome = new MutableGenome(this.getBuildName());
    for (Chromosome chr : this.getChromosomes())
      {
      try
        {
        newGenome.addChromosome(this.mutate(chr, window, writer));
        }
      catch (IOException e)
        {
        log.error(e);
        }
      }
    return newGenome;
    }


  /**
   * Loop over the sequence, mutate and immediately write to new FASTA file.
   * This does not keep the new chromosome in memory.
   *
   * @param chr
   * @param window
   * @param writer
   * @throws IOException
   */
  public Chromosome mutate(Chromosome chr, int window, FASTAWriter writer) throws IOException
    {
    // TODO so...the FASTEST thing to do here would be to mutate ever section concurrently, write to a temp file that is labeled appropriately and put it
    // back together in order
    int total = 0;
    log.debug(chr.getName());

    Location location = new Location(0, window); // FASTA locations are 0 based.
    chr.getFASTAReader().reset();

    DNASequence currentSequenceFragment;

    while (true)
      {
      currentSequenceFragment = chr.readSequence(window);
      log.debug(chr.getName() + location.toString());
      DNASequence mutatedSequence = mutateSequenceAtLocation(chr, currentSequenceFragment, location);
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
    writer.flush();
    writer.close();
    log.info("Mutated chromosome " + chr.getName() + " sequence length " + total);
    return new Chromosome(chr.getName(), writer.getFASTAFile());
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
        log.info("MUTATING FRAGMENT " + fragment.toString());
        // apply the variations to the sequence, each of them needs to apply to the same fragment
        // it is possible that one could override another (e.g. a deletion removes SNVs)
        for (Variation variation : this.getVariantTypes())
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

  private void writeVariations(Chromosome chr, Location fragment, Variation variation, Map<Location, DNASequence> mutations)
    {
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
