package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.Variation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

public abstract class Genome
    {
    protected HashMap<String, Chromosome> Chromosomes;
    private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

    protected Genome()
      {
      Chromosomes = new HashMap<String, Chromosome>();
      }

    protected Genome(Collection<Chromosome> chromosomes)
      {
      this();
      for (Chromosome c: chromosomes) { Chromosomes.put(c.getName(), c); }
      }

    public void addChromosome(Chromosome chr)
      {
      this.Chromosomes.put(chr.getName(), chr);
      }

    public boolean hasChromosome(String name)
      {
      return Chromosomes.containsKey(name);
      }

    public Chromosome[] getChromosomes()
      {
      return Chromosomes.values().toArray( new Chromosome[Chromosomes.size()]);
      }

    public void addVariationType(Variation v, ProbabilityList pl)
      {
      this.variationProbabilities.put(v, pl);
      }


    /* TODO Not really sure how this should work in the actual code yet. */
    // one thing, each chromosome can be mutated concurrently in the reference genome as theere will
    // not be any translocations.  I don't actually think I know how to do that...
    //public abstract void mutateChromosome(String chr, Location loc);
    }
