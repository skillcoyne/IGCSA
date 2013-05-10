package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.*;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.concurrency.SmallMutable;

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

  // Database connections
  private GCBinDAO binDAO;
  private FragmentDAO fragmentDAO;

  // Directories to output to
  private File genomeDirectory;
  private File smallMutDir;


  public MutableGenome(GCBinDAO gcBinDAO, FragmentDAO fragmentDAO)
    {
    this.binDAO = gcBinDAO;
    this.fragmentDAO = fragmentDAO;
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

  public File getGenomeDirectory()
    {
    return genomeDirectory;
    }

  public void setGenomeDirectory(File genomeDirectory)
    {
    this.genomeDirectory = genomeDirectory;
    }

  public File getSmallMutationDirectory()
    {
    return smallMutDir;
    }

  public void setMutationDirectories(File smallMut)
    {
    this.smallMutDir = smallMut;
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
    if (chromosomes.containsKey(chr.getName()))
      this.addChromosome(chr);
    else
      log.warn("No chromosome " + chr.getName() + " to be replaces. Added instead.");
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
      MutationWriter mutWriter = new MutationWriter(new File(smallMutDir, chr.getName() + "mutations.txt"), MutationWriter.SMALL);

      chr.setMutationsFile(writer.getFASTAFile());

      SmallMutable m = new SmallMutable(chr, window);
      m.setConnections(binDAO, fragmentDAO);
      m.setWriters(writer, mutWriter);
      return m;
      }
    catch (IOException e)
      {
      throw new RuntimeException(e);
      }
    }

  }
