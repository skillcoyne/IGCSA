package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.variation.Variation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Chromosome
  {
  private Sequence Sequence;
  private int Length;
  private String Name;
  private Collection<Variation> Variations;

  public Chromosome(String name, String sequence) throws Exception
    {
    this.Name = name; this.Sequence = new Sequence(sequence); this.Length = sequence.length();
    this.Variations = new ArrayList<Variation>();
    }

  public String getSequence()
    {
    return Sequence.toString();
    }

  public int getLength()
    {
    return Length;
    }

  public String getName()
    {
    return Name;
    }

  public void addVariation(Variation var)
    {
    Variations.add(var); if (var.getLocation().getEnd() > Length) { Length += Length - var.getLocation().getEnd(); }
    }

  }
