package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
import java.io.FileNotFoundException;
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
  private DNASequence DNASequence;
  private int Length;
  private String Name;
  private File fasta;

  private Collection<Variation> Variations = new ArrayList<Variation>();


  protected Chromosome(String name, int length)
    {
    this.Name = name;
    this.Length = length;
    }

  public Chromosome(String name, File chrFastaFile)
    {
    this(name, 0);
    this.fasta = chrFastaFile;
    try
      {
      FASTAReader reader = new FASTAReader(this.fasta);
      this.Length = reader.sequenceLength();
      }
    catch (Exception e)
      {
      e.printStackTrace();
      }
    }

  public Chromosome(String name, String sequence) throws Exception
    {
    this(name, sequence.length());
    this.DNASequence = new DNASequence(sequence);
    }

  public String getDNASequence()
    {
    return DNASequence.getSequence();
    }

  public String getName()
    {
    return Name;
    }

  public File getFASTAFile()
    {
    return fasta;
    }

  public String toString()
    {
    return Name;
    }

  }
