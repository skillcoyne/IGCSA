package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Chromosome
  {
  private Sequence Sequence;
  private int Length;
  private String Name;
  private Collection<Variation> Variations;
  private File fasta;

  protected Chromosome(String name, int length)
    {
    this.Name = name;
    this.Length = length;
    this.Variations = new ArrayList<Variation>();
    }

  public Chromosome(String name, File chrFastaFile)
    {
    this(name, 0);
    this.fasta = chrFastaFile;
    }

  public Chromosome(String name, String sequence) throws Exception
    {
    this(name, sequence.length());
    this.Sequence = new Sequence(sequence);
    }

  public Sequence getSequence()
    {
    return Sequence;
    }

  public String getName()
    {
    return Name;
    }

  public File getFASTAFile()
    {
    return fasta;
    }

  public void addVariation(Variation var)
    {
    Variations.add(var); if (var.getLocation().getEnd() > Length) { Length += Length - var.getLocation().getEnd(); }
    }

  public String toString()
    {
    return Name;
    }

  }
