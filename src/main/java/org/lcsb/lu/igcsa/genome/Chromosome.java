package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Chromosome
  {
  private String Name;
  private File fasta;
  private FASTAReader reader;
  private Map<Location, DNASequence> alteredSequence = new HashMap<Location, DNASequence>();

  public Chromosome(String name)
    {
    this.Name = name;
    }

  public Chromosome(String name, File chrFastaFile)
    {
    this(name);
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

  public String getName()
    {
    return Name;
    }

  public File getFASTA()
    {
    return this.fasta;
    }

  public FASTAReader getFASTAReader()
    {
    return this.reader;
    }

  public String toString()
    {
    return Name;
    }

  /**
   * Returns the sequence at the given location. If there is a mutated sequence
   * for the given location it returns that. That location has to be exactly the same
   * however.  This could be problematic so ...
   * Otherwise reads it from the FASTA file.
   * @param loc
   * @return
   */
  public String getSequence(Location loc)
    {
    String sequence = null;
    if (this.alteredSequence.containsKey(loc))
      { sequence = this.alteredSequence.get(loc).getSequence(); }
    else
      {
      try { sequence = reader.readSequenceFromLocation(loc.getStart(), loc.getLength()); }
      catch (IOException e) { e.printStackTrace(); }
      }
    return sequence;
    }

  /**
   * Get sequence in chunks from the FASTA file.  Each call will read sequentially from last call.
   * To get specific regions or any mutated sequences use Chromosome#getSequence(Location)
   *
   * @param window
   * @return
   */
  public DNASequence readSequence(int window)
    {
    DNASequence sequence = null;
    try
      {
      long start = reader.getLastLocation();
      sequence = new DNASequence(reader.readSequence(window), new Location((int) start, (int) reader.getLastLocation()));
      }
    catch (IOException ioe)
      { ioe.printStackTrace(); }
    return sequence;
    }

  public void alterSequence(Location loc, DNASequence sequence)
    {
    this.alterSequence(loc, sequence);
    }

  }
