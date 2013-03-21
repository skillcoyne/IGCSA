package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import static org.lcsb.lu.igcsa.genome.Nucleotides.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class DNASequence
  {
  static Logger log = Logger.getLogger(DNASequence.class.getName());

  private String sequence = "";
  private Location location;



  // TODO Need to return a location to start mutations from in the case of gaps or unknown nucleotides. -- maybe


  private void testNucleotides(String sequence) throws IllegalArgumentException
    {
    if (sequence.length() <= 0) throw new IllegalArgumentException("No sequence provided.");
    if (!validCharacters().matcher(sequence).matches())
      throw new IllegalArgumentException("The sequence contains incorrect nucleotides, expected " + validCharacters() + " provided: " + sequence);
    }

  public DNASequence()
    {}

  public DNASequence(String seq) throws IllegalArgumentException
    {
    seq = seq.toUpperCase();
    testNucleotides(seq);
    this.sequence = seq;
    }

  public DNASequence(String seq, Location location) throws IllegalArgumentException
    {
    this(seq);
    this.location = location;
    }

  public int calculateGC()
    {
    int guanine = StringUtils.countOccurrencesOf(this.sequence, G.toString());
    int cytosine = StringUtils.countOccurrencesOf(this.sequence, C.toString());
    return (guanine + cytosine);
    }

  public int calculateAT()
    {
    int thymidine = StringUtils.countOccurrencesOf(this.sequence, T.toString());
    int adenine = StringUtils.countOccurrencesOf(this.sequence, A.toString());
    return (thymidine + adenine);
    }

  public void addNucleotides(String nuc) throws IllegalArgumentException
    {
    nuc = nuc.toUpperCase();
    testNucleotides(nuc);
    this.sequence = sequence + nuc;
    }

  public void merge(DNASequence seq)
    {
    this.sequence = this.sequence + seq;
    }

  public int getLength()
    {
    return sequence.length();
    }

  public String getSequence()
    {
    return sequence;
    }

  public Location getLocation()
    {
    return this.location;
    }

  public int calculateNucleotides()
    {
    return (calculateAT() + calculateGC());
    }

  // Including gaps
  public int calculateUnknown()
    {
    int unk = StringUtils.countOccurrencesOf(this.sequence, UNKNOWN.toString());
    int gap = StringUtils.countOccurrencesOf(this.sequence, GAP.toString());
    return (unk + gap);
    }


  @Override
  public String toString()
    {
    return this.getSequence();
    }
  }
