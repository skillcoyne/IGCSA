package org.lcsb.lu.igcsa.genome;

import org.springframework.util.StringUtils;
import sun.misc.Regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class DNASequence
  {
  private String sequence = "";
  private Location location;
  private final Pattern nucleotides = Pattern.compile("([ACTGNactgn-]+)");
  private final Pattern unknown = Pattern.compile("[Nn]+");
  private final Pattern gap = Pattern.compile("[-]+");



  private void testNucleotides(String sequence) throws IllegalArgumentException
    {
    Matcher match = nucleotides.matcher(sequence);
    if (!match.matches())
      throw new IllegalArgumentException("The sequence contains incorrect nucleotides, expected " + nucleotides + " provided: " + sequence);
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
    int guanine = StringUtils.countOccurrencesOf(this.sequence, "G");
    int cytosine = StringUtils.countOccurrencesOf(this.sequence, "C");
    return (guanine + cytosine);
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

//  public boolean hasUnknownNucleotides()
//    {
//    Matcher match = unknown.matcher(sequence);
//    return match.matches();
//    }
//
//  public boolean hasGaps()
//    {
//    Matcher match = gap.matcher(sequence);
//    return match.matches();
//    }

  @Override
  public String toString()
    {
    return this.getSequence();
    }
  }
