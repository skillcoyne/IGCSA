package org.lcsb.lu.igcsa.genome;

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
  private String Sequence;
  private final Pattern nucleotides = Pattern.compile("([ACTGNactgn]+)");

  private void testNucleotides(String sequence) throws IllegalArgumentException
    {
    Matcher match = nucleotides.matcher(sequence);
    if (!match.matches())
      throw new IllegalArgumentException("The sequence contains incorrect nucleotides, expected " + nucleotides + " provided: " + sequence);
    }

  public DNASequence(String seq) throws IllegalArgumentException
    {
    seq = seq.toUpperCase();
    testNucleotides(seq);
    this.Sequence = seq;
    }

  public void addNucleotides(String nuc) throws IllegalArgumentException
    {
    nuc = nuc.toUpperCase();
    testNucleotides(nuc);
    this.Sequence = Sequence + nuc;
    }

  public int getLength()
    {
    return Sequence.length();
    }

  public String getSequence()
    {
    return Sequence;
    }

  }
