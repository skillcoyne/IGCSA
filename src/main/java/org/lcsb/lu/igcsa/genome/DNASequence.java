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
  private String testNucleotides(String sequence) throws IllegalArgumentException
    {
    if (sequence.length() > 0 && !validCharacters().matcher(sequence).matches())
      {
      log.warn("The sequence contains incorrect nucleotides, expected " + validCharacters() + " provided: " + sequence);
      sequence = "";
      }
    return sequence;
    }

  public DNASequence()
    {}

  public DNASequence(String seq) throws IllegalArgumentException
    {
    if (seq == null) seq = "";
    seq = seq.replaceAll("\\s", "");
    seq = seq.toUpperCase();
    this.sequence = testNucleotides(seq);
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
    this.sequence = sequence + testNucleotides(nuc);
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
