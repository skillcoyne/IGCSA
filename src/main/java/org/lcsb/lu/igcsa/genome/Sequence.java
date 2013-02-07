package org.lcsb.lu.igcsa.genome;

import sun.misc.Regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * org.lcsb.lu.igcsa.genome
 * User: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open source License Apache 2
 */
public class Sequence
  {
  private String Sequence;
  private final Pattern nucleotides = Pattern.compile("([actgN]+)");

  private void testNucleotides(String sequence) throws Exception
    {
    Matcher match = nucleotides.matcher(sequence);
    if (!match.matches())
      {
      throw new Exception("The sequence contains incorrect nucleotides, expected " + nucleotides + " provided: " +
                      sequence);
      }

    }

  public Sequence(String seq) throws Exception
    {
    testNucleotides(seq);
    this.Sequence = seq;
    }

  public void addNucleotides(String nuc) throws Exception
    {
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
