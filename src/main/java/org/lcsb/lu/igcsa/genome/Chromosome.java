package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
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
public class Chromosome
  {
  static Logger log = Logger.getLogger(Chromosome.class.getName());

  private String Name;
  private File fasta;
  private FASTAReader reader;
  private List<Variation> variationList;

  private NavigableMap<Location, DNASequence> alteredSequence = new TreeMap<Location, DNASequence>();

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
      log.error(e);
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

  public void setVariantList(List<Variation> variants)
    {
    this.variationList = variants;
    log.debug(getName() + "variant list: " + variationList.hashCode());
    }

  public List<Variation> getVariantList()
    {
    return this.variationList;
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
   *
   * @param loc
   * @return
   */
//  public DNASequence getSequence(Location loc)
//    {
//    String sequence = null;
//    if (this.alteredSequence.containsKey(loc))
//      {
//      sequence = this.alteredSequence.get(loc).getSequence();
//      }
//    else
//      {
//      try
//        {
//        sequence = reader.readSequenceFromLocation(loc.getStart(), loc.getLength());
//        }
//      catch (IOException e)
//        {
//        e.printStackTrace();
//        }
//      }
//    return new DNASequence(sequence);
//    }

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
      {
      ioe.printStackTrace();
      }
    return sequence;
    }

  /**
   * This currently gets only the sequence from the FASTA file.  The sequences in "alterSequence" are not used for anything right now.
   *
   * @return
   */
  public DNASequence retrieveFullSequence()
    {
    log.debug("Retrieve full sequence from " + this.fasta.getAbsolutePath());
    DNASequence fullSequence = new DNASequence();
    try
      {
      reader.reset();
      }
    catch (IOException e)
      {
      log.error(e);
      }

    //this.fullSequence = new DNASequence();
    int window = 500;
    String currentSeq;
    while (true)
      {
      currentSeq = this.readSequence(window).getSequence();
      fullSequence.addNucleotides(currentSeq);
      if (currentSeq.length() < window) break;
      }
    return fullSequence;
    }


  /*
 I'm not certain this is a useful thing yet.
  */
  public void alterSequence(Location loc, DNASequence sequence)
    {
    this.alteredSequence.put(loc, sequence);
    }

  //  private void mergeAlteredSequences()
  //    {
  //    String sequence = retrieveFullSequence().getSequence();
  //
  //    int currentIndex = 0;
  //    String newSequence = "";
  //    // Basically if the beginning of the sequence hasn't been altered...
  //    if (currentIndex < alteredSequence.firstEntry().getKey().getStart())
  //      {
  //      newSequence = sequence.substring(0, alteredSequence.firstEntry().getKey().getStart());
  //      currentIndex = alteredSequence.firstEntry().getKey().getStart();
  //      }
  //
  //    for(Map.Entry<Location, DNASequence> entry: this.alteredSequence.entrySet())
  //      {
  //      log.info(entry.getKey() + " : " + entry.getValue());
  //
  //      Location current = entry.getKey();
  //
  //      newSequence = newSequence + sequence.substring(current.getStart(), current.getEnd());
  //
  //
  //      }
  //    }


  }
