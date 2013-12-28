package org.lcsb.lu.igcsa.genome;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public enum Nucleotides
  {
  A('A'),
  C('C'),
  T('T'),
  G('G'),
  U('U'),
  R('R'),
  Y('Y'),
  K('K'),
  M('M'),
  S('S'),
  W('W'),
//  B('B'),	//not A (i.e. C, G, T or U)	B comes after A
//  D('D'),	//not C (i.e. A, G, T or U)	D comes after C
//  H('H'),	//not G (i.e., A, C, T or U)	H comes after G
//  V('V'),	//neither T nor U (i.e. A, C or G)	V comes after U
  UNKNOWN('N'),
  MASKED('X'),
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

  public static String all()
    {
    return StringUtils.join(Nucleotides.values());
    }

  public static Pattern validCharacters()
    {
    String regex = Nucleotides.all() + Nucleotides.all().toLowerCase();
    return Pattern.compile("([" + regex + "]+)");
    //return Pattern.compile("([ACTGNXMRYKactgnmxryk-]+)");
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
