package org.lcsb.lu.igcsa.genome;

import java.util.HashMap;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public enum Nucleotides
  {
  A('A', "adenosine"),
  C('C', "cytosine"),
  G('G', "guanine"),
  T('T', "thymidine"),
  N('N', "any"),
//  U("U", "uridine"),
//  K("G/T", "keto"),
//  S("G/C", "strong"),
//  Y("T/C", "pyrimidine"),
//  M("A/C", "amino"),
//  W("A/T", "weak"),
//  R("G/A", "purine"),
//  B("G/T/C", ""),
//  D("G/A/T", ""),
//  H("A/C/T", ""),
//  V("G/C/A", ""),
  GAP('-', "gap of indeterminate length");

  private final char nucleotide;
  private final String description;

  Nucleotides(char n, String d)
    {
    this.nucleotide = n;
    this.description = d;
    }

  public char getNucleotide()
    {
    return this.nucleotide;
    }

  public String getDescription()
    {
    return this.description;
    }

  public int getCharValue()
    {
    return Character.getNumericValue(this.nucleotide);
    }

  public static char[] validDNA()
    {
    char[] dna = {A.getNucleotide(), C.getNucleotide(), T.getNucleotide(), G.getNucleotide()};
    return dna;
    }

  }
