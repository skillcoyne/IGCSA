package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.*;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
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
  private List<Variation> variantTypes;

  // Database connections
  private GCBinDAO binDAO;
  private FragmentVariationDAO variationDAO;
  private SizeDAO sizeDAO;

  public MutableGenome(GCBinDAO gcBinDAO, FragmentVariationDAO variationDAO, SizeDAO sizeDAO)
    {
    this.binDAO = gcBinDAO;
    this.variationDAO = variationDAO;
    this.sizeDAO = sizeDAO;
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
//  public Genome mutate(int window, FASTAWriter writer)
//    {
//    ExecutorService executorService = Executors.newFixedThreadPool(5);
//
//    Genome newGenome = new MutableGenome(this.getBuildName());
//    for (Chromosome chr : this.getChromosomes())
//      {
//      newGenome.addChromosome(new Chromosome(chr.getName(), writer.getFASTAFile()));
//      executorService.execute(this.mutate(chr, window, writer));
//      }
//    executorService.shutdown();
//    return newGenome;
//    }


  public Mutable mutate(Chromosome chr, int window, FASTAWriter writer)
    {
    try
      {
      // TODO variant types need to be cloned for each chromosome or else the mutations being generated are incorrect MAJOR PROBLEM
      Mutable m = new Mutable(chr, window);

      m.setConnections(binDAO, variationDAO, sizeDAO);
      m.setWriters(writer, new MutationWriter(new File(writer.getFASTAFile().getParentFile().getAbsolutePath(), chr.getName() + "mutations.txt")));
      return m;
      }
    catch (IOException e)
      {
      throw new RuntimeException(e);
      }
    }




  }
