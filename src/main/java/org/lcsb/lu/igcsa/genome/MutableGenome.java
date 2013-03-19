package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Bin;
import org.lcsb.lu.igcsa.database.Fragment;
import org.lcsb.lu.igcsa.database.FragmentVariationDAO;
import org.lcsb.lu.igcsa.database.GCBinDAO;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
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

  private List<Variation> variantTypes;

  public MutableGenome(GCBinDAO gcBinDAO, FragmentVariationDAO variationDAO)
    {
    this.binDAO = gcBinDAO;
    this.variationDAO = variationDAO;
    }

  protected MutableGenome(String buildName)
    {
    this.buildName = buildName;
    }

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


  public Chromosome mutate(Chromosome chr, int window)
    {
    Chromosome mutatedChr = chr;

    DNASequence currentSequenceFragment;
    Location location = new Location(0, window); // FASTA locations are 0 based.
    while ((currentSequenceFragment = chr.getSequence(location)) != null)
      {
      DNASequence mutatedSequence = mutateSequenceAtLocation(chr, location, currentSequenceFragment);
      mutatedChr.alterSequence(location, mutatedSequence);
      }
    return mutatedChr;
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
    log.debug(chr.getName());
    Location location = new Location(0, window); // FASTA locations are 0 based.

    DNASequence currentSequenceFragment;
    while ((currentSequenceFragment = chr.getSequence(location)) != null)
      {
      DNASequence mutatedSequence = mutateSequenceAtLocation(chr, location, currentSequenceFragment);
      try
        {
        writer.writeLine(mutatedSequence.getSequence());
        }
      catch (IOException e)
        {
        log.error(e);
        }
      writer.flush();
      }
    writer.close();
    return new Chromosome(chr.getName(), writer.getFASTAFile());
    }

  // Mutates the sequence based on the information provided in the database
  private DNASequence mutateSequenceAtLocation(Chromosome chr, Location location, DNASequence sequence)
    {
    DNASequence mutatedSequence = sequence;

    // get the GC content in order to select the correct fragment bin
    int gcContent = sequence.calculateGC();
    if (gcContent > 0)
      {
      Bin gcBin = this.binDAO.getBinByGC(chr.getName(), gcContent);

      // get random fragment within the GC bin
      Random randomFragment = new Random();
      Fragment fragment = this.variationDAO.getFragment(chr.getName(), gcBin.getBinId(), randomFragment.nextInt(gcBin.getSize()));
      log.debug(fragment.toString());

      // apply the variations to the sequence, each of them needs to apply to the same fragment
      // it is possible that one could override another (e.g. a deletion removes SNVs)
      for (Variation variation : this.getVariantTypes())
        {
        variation.setMutationFragment(fragment);
        mutatedSequence = variation.mutateSequence(mutatedSequence);
        }
      }
    return mutatedSequence;
    }


  }
