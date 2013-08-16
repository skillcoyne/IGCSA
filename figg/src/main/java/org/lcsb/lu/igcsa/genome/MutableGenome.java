package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.*;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.concurrency.FragmentMutable;


import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class MutableGenome extends Genome
  {
  static Logger log = Logger.getLogger(Genome.class.getName());

  // Database connections
  private GCBinDAO binDAO;
  private FragmentDAO fragmentDAO;

  public MutableGenome(GCBinDAO gcBinDAO, FragmentDAO fragmentDAO)
    {
    this.binDAO = gcBinDAO;
    this.fragmentDAO = fragmentDAO;
    }

  @Override
  public MutableGenome copy()
    {
    MutableGenome g = new MutableGenome(this.binDAO, this.fragmentDAO);
    g.setBuildName(this.buildName);
    g.setGenomeDirectory(this.getGenomeDirectory());
    for (Chromosome chr: this.getChromosomes())
      g.addChromosome(chr);
    g.setMutationDirectory(this.getMutationDirectory());

    return g;
    }

  public FragmentMutable mutate(Chromosome chr, int window, FASTAWriter writer)
    {
    try
      {
      MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, chr.getName() + "mutations.txt"), MutationWriter.SMALL);

      chr.setMutationsFile(writer.getFASTAFile());

      FragmentMutable m = new FragmentMutable(chr, window);
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
