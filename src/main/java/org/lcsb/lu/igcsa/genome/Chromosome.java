package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
  private FASTAReader reader;

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
      this.reader = new FASTAReader(this.fasta);
      }
    catch (Exception e)
      {
      e.printStackTrace();
      }
    }


  public Chromosome(String name, DNASequence sequence)
    {
    this.DNASequence = sequence;
    }

  public Chromosome(String name, String sequence)
    {
    this(name, sequence.length());
    this.DNASequence = new DNASequence(sequence);
    }


  public DNASequence getDNASequence()
    {
    return DNASequence;
    }

  /**
   * Get sequence in chunks.  Each call will read from the end point of the last call to the method.
   * @param window
   * @return
   */
  public String getDNASequence(int window)
    {
    String sequenceChunk = "";
    if (DNASequence != null)
      {
      // loop through and return it in chunks
      }
    else
      {
      //if I"m getting the sequence from the FASTAreader I'm not so sure I want to keep it all in memory...
      try
        { sequenceChunk = reader.readSequence(window); }
      catch (IOException ioe)
        { ioe.printStackTrace(); }
      return sequenceChunk;
      }
    return sequenceChunk;
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
