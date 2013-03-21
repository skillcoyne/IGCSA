package org.lcsb.lu.igcsa.genome;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public enum Nucleotides
  {
//  public static final String A = "A";
//  public static final String C = "C";
//  public static final String G = "G";
//  public static final String T = "T";
//  public static final char GAP= '-' ;
//  public static final char UNKNOWN = 'N';
//
//  static final Pattern nucleotides = Pattern.compile("([ACTGNactgn-]+)");
//  static final Pattern unknown = Pattern.compile("[Nn]+");
//  static final Pattern gap = Pattern.compile("[-]+");


  A('A'),
  C('C'),
  T('T'),
  G('G'),
  UNKNOWN('N'),
  GAP('-');

  private final char nucleotide;

  private Nucleotides(char n)
    {
    this.nucleotide = n;
    }

  public char value()
    {
    return this.nucleotide;
    }


  public String toString()
    {
    return String.valueOf(this.nucleotide);
    }

  public static Pattern validCharacters()
    {
    return Pattern.compile("([ACTGNactgn-]+)");
    }

  public static Pattern unknownNucleotides()
    {
    return Pattern.compile("[Nn]+");
    }

  public static Pattern sequenceGap()
    {
    return Pattern.compile("[-]+");
    }



  //  A('A', "adenosine"),
//  C('C', "cytosine"),
//  G('G', "guanine"),
//  T('T', "thymidine"),
//  N('N', "any"),
////  U("U", "uridine"),
////  K("G/T", "keto"),
////  S("G/C", "strong"),
////  Y("T/C", "pyrimidine"),
////  M("A/C", "amino"),
////  W("A/T", "weak"),
////  R("G/A", "purine"),
////  B("G/T/C", ""),
////  D("G/A/T", ""),
////  H("A/C/T", ""),
////  V("G/C/A", ""),
//  GAP('-', "gap of indeterminate length");
//
//  private final char nucleotide;
//  private final String description;
//
//  Nucleotides(char n, String d)
//    {
//    this.nucleotide = n;
//    this.description = d;
//    }
//
//  public char getNucleotide()
//    {
//    return this.nucleotide;
//    }
//
//  public String getDescription()
//    {
//    return this.description;
//    }
//
//  public int getCharValue()
//    {
//    return Character.getNumericValue(this.nucleotide);
//    }
//
//  public static char[] validDNA()
//    {
//    char[] dna = {A.getNucleotide(), C.getNucleotide(), T.getNucleotide(), G.getNucleotide()};
//    return dna;
//    }

  }
