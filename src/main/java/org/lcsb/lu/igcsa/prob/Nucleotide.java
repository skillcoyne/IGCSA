package org.lcsb.lu.igcsa.prob;

import org.lcsb.lu.igcsa.fasta.NucleotideCodes;

import java.util.HashMap;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

// Singleton class?
public class Nucleotide
  {
  private char nucleotide;
  private Map<Character, Probability> mutateTo = new HashMap<Character, Probability>();

  public Nucleotide(char n)
    {
    this.nucleotide = n;
    }

  public Map<Character, Probability> getProbabilities()
    {
    return this.mutateTo;
    }

  public char getNucleotide()
    {
    return this.nucleotide;
    }

  public void toNucelotide(char n, Probability p)
    {
    mutateTo.put(n, p);
    }

  public Probability probabilityOf(char n)
    {
    return mutateTo.get(n);
    }

  }
