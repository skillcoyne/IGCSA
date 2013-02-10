package org.lcsb.lu.igcsa.genome;

import java.util.Collection;
import java.util.HashMap;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

public class Genome
    {
    protected HashMap<String, Chromosome> Chromosomes;

    public Genome()
      {
      Chromosomes = new HashMap<String, Chromosome>();
      }

    public Genome(Collection<Chromosome> chromosomes)
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

    public Collection<Chromosome> getChromosomes()
      {
      return Chromosomes.values();
      }

    /* TODO Not really sure how this should work in the actual code yet. */
    // one thing, each chromosome can be mutated concurrently in the reference genome as theere will
    // not be any translocations.  I don't actually think I know how to do that...
    public void mutateChromosome(String chr, Location loc)
      {

      }


    }
