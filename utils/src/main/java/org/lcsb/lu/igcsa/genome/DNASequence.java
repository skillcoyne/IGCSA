package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import static org.lcsb.lu.igcsa.genome.Nucleotides.*;

/**
 * org.lcsb.lu.igcsa.tables
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class DNASequence
  {
  static Logger log = Logger.getLogger(DNASequence.class.getName());

  private String sequence = "";
  private Location location;

  // Might be useful to return a location to start mutations from in the case of gaps or unknown nucleotides.
  private String testNucleotides(String sequence) throws IllegalArgumentException
    {
    sequence = sequence.trim();
    if (sequence.length() > 0 && !Nucleotides.validCharacters().matcher(sequence).matches())
      {
      log.warn("The sequence contains incorrect nucleotides, expected " + Nucleotides.validCharacters() + " provided: " + sequence);
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
    int guanine = StringUtils.countOccurrencesOf(this.sequence, Nucleotides.G.toString());
    int cytosine = StringUtils.countOccurrencesOf(this.sequence, Nucleotides.C.toString());
    return (guanine + cytosine);
    }

  public int calculateAT()
    {
    int thymidine = StringUtils.countOccurrencesOf(this.sequence, Nucleotides.T.toString());
    int adenine = StringUtils.countOccurrencesOf(this.sequence, Nucleotides.A.toString());
    return (thymidine + adenine);
    }

  public void addNucleotides(String nuc) throws IllegalArgumentException
    {
    nuc = nuc.toUpperCase();
    this.sequence = sequence + testNucleotides(nuc);
    }

  public void merge(DNASequence dnaSequence)
    {
    this.sequence = this.sequence + dnaSequence.toString();
    }

  public void merge(String seq)
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
    int unk = StringUtils.countOccurrencesOf(this.sequence, Nucleotides.UNKNOWN.toString());
    int gap = StringUtils.countOccurrencesOf(this.sequence, Nucleotides.GAP.toString());
    return (unk + gap);
    }


  @Override
  public boolean equals(Object o)
    {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DNASequence sequence1 = (DNASequence) o;

    if (sequence != null ? !sequence.equals(sequence1.sequence) : sequence1.sequence != null)
      return false;

    return true;
    }

  @Override
  public int hashCode()
    {
    return sequence != null ? sequence.hashCode() : 0;
    }

  @Override
  public String toString()
    {
    return this.getSequence();
    }
  }
