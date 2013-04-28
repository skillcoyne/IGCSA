package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.*;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

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

  // Directory to output to
  private File genomeDirectory;

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

  public File getGenomeDirectory()
    {
    return genomeDirectory;
    }

  public void setGenomeDirectory(File genomeDirectory)
    {
    this.genomeDirectory = genomeDirectory;
    }

  public void addChromosome(Chromosome chr)
    {
    this.chromosomes.put(chr.getName(), chr);
    log.info("Added chromosome " + chr.getName() + ", have " + chromosomes.size() + " chromosomes");
    }

  public void addChromosomes(Chromosome[] chromosomes)
    {
    for (Chromosome chromosome : chromosomes)
      this.addChromosome(chromosome);
    }


  public void replaceChromosome(Chromosome chr)
    {
    if (chromosomes.containsKey(chr.getName())) this.addChromosome(chr);
    else log.warn("No chromosome " + chr.getName() + " to be replaces. Added instead.");
    }

  public boolean hasChromosome(String name)
    {
    return chromosomes.containsKey(name);
    }

  public Chromosome[] getChromosomes()
    {
    return chromosomes.values().toArray(new Chromosome[chromosomes.size()]);
    }

  public Chromosome getChromosome(String name)
    {
    return chromosomes.get(name);
    }

  public SmallMutable mutate(Chromosome chr, int window, FASTAWriter writer)
    {
    try
      {
      // TODO variant types need to be cloned for each chromosome or else the mutations being generated are incorrect MAJOR PROBLEM
      SmallMutable m = new SmallMutable(chr, window);

      m.setConnections(binDAO, variationDAO, sizeDAO);

      File mutWriterPath = new File(writer.getFASTAFile().getParentFile().getAbsolutePath(), "mutations");
      if (!mutWriterPath.exists() || !mutWriterPath.isDirectory()) mutWriterPath.mkdir();

      m.setWriters(writer, new MutationWriter(new File(mutWriterPath, chr.getName() + "mutations.txt")));
      return m;
      }
    catch (IOException e)
      {
      throw new RuntimeException(e);
      }
    }

  public StructuralMutable mutate(Chromosome chr, FASTAWriter writer)
    {
    try
      {
      StructuralMutable m = new StructuralMutable(chr);

      File mutWriterPath = new File(writer.getFASTAFile().getParentFile().getAbsolutePath(), "structural-variations");
      if (!mutWriterPath.exists() || !mutWriterPath.isDirectory()) mutWriterPath.mkdir();
      m.setWriters(writer, new MutationWriter(new File(mutWriterPath, chr.getName() + "mutations.txt")));
      return m;
      }
    catch (IOException e)
      {
      throw new RuntimeException(e);
      }
    }
  }
