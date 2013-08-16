package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.SequenceAberration;

import java.util.*;


/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This class will work for all Karyotype chromosomes EXCEPT translocations.
 */

public class DerivativeChromosome extends Chromosome
  {
  static Logger log = Logger.getLogger(DerivativeChromosome.class.getName());

  private List<SequenceAberration> sequenceAberrationList = new ArrayList<SequenceAberration>();

  private Set<Chromosome> chromosomes = new HashSet<Chromosome>();

  public DerivativeChromosome(String name)
    {
    super(name);
    }

  public DerivativeChromosome(String name, Chromosome chr)
    {
    super(name);
    this.chromosomes.add(chr);
    }

  public DerivativeChromosome(String name, Chromosome[] chromosomes)
    {
    super(name);
    for (Chromosome c: chromosomes)
      this.chromosomes.add(c);
    }

  // Currently only used for Translocatios
  public void addChromosome(Chromosome chr)
    {
    this.chromosomes.add(chr);
    }

  public Collection<Chromosome> getChromosomes()
    {
    return this.chromosomes;
    }

  public void addSequenceAberration(SequenceAberration abr)
    {
    sequenceAberrationList.add(abr);
    }

  public void setSequenceAberrationList(List<SequenceAberration> sequenceAberrations)
    {
    sequenceAberrationList = sequenceAberrations;
    }

  public void addAberration(SequenceAberration abr)
    {
    sequenceAberrationList.add(abr);
    }

  public List<SequenceAberration> getSequenceAberrationList()
    {
    return sequenceAberrationList;
    }


  }
