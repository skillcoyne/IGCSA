package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.Mutation;
import org.lcsb.lu.igcsa.database.normal.*;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.prob.Frequency;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  private List<Variation> variantTypes;
  private Map<String, Frequency> sizeFreqMap = new HashMap<String, Frequency>();

  // Database connections
  private GCBinDAO binDAO;
  private FragmentVariationDAO variationDAO;
  private SizeDAO sizeDAO;


  private MutationWriter mutationWriter;

  public MutableGenome(GCBinDAO gcBinDAO, FragmentVariationDAO variationDAO, SizeDAO sizeDAO)
    {
    this.binDAO = gcBinDAO;
    this.variationDAO = variationDAO;
    this.sizeDAO = sizeDAO;
    }

  public void setMutationWriter(MutationWriter writer)
    {
    this.mutationWriter = writer;
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
    for (Variation var : variantTypes)
      var.setSizeVariation(this.getSizeFrequencies());
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
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    //int genomeId = this.genomeDAO.insertGenome(id, writer.getFASTAFile().getAbsolutePath());

    Genome newGenome = new MutableGenome(this.getBuildName());
    for (Chromosome chr : this.getChromosomes())
      {
      newGenome.addChromosome(new Chromosome(chr.getName(), writer.getFASTAFile()));
      executorService.execute(this.mutate(chr, window, writer));
      }
    executorService.shutdown();
    return newGenome;
    }


  public Mutable mutate(Chromosome chr, int window, FASTAWriter writer)
    {
    try
      {
      // TODO variant types need to be cloned for each chromosome or else the mutations being generated are incorrect MAJOR PROBLEM
      Mutable m = new Mutable(chr, window, cloneVariantTypes());

      m.setConnections(binDAO, variationDAO, sizeDAO);
      m.setWriters(writer, new MutationWriter(new File(writer.getFASTAFile().getParentFile().getAbsolutePath(), chr.getName() + "mutations.txt")));
      return m;
      }
    catch (IOException e)
      {
      throw new RuntimeException(e);
      }
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
          log.info(variation.getVariationName());

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

  private Map<String, Frequency> getSizeFrequencies()
    {
    if (this.sizeFreqMap.size() <= 0)
      {
      try
        {
        this.sizeFreqMap = this.sizeDAO.getAll();
        }
      catch (ProbabilityException e)
        {
        log.error(e);
        }
      }
    return this.sizeFreqMap;
    }

  private List<Variation> cloneVariantTypes()
    {
    // TODO...this just does a shallow clone.  Need to provide a complete copy to each chromosome...somehow.
    List<Variation> cloneList = new ArrayList<Variation>(getVariantTypes());
    return cloneList;
    }


  }
