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

public abstract class AbstractGenome implements Genome
    {
    protected String buildName;
    protected HashMap<String, Chromosome> Chromosomes;
    private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

    public AbstractGenome()
      {
      Chromosomes = new HashMap<String, Chromosome>();
      }

    public AbstractGenome(String build)
      {
      this();
      this.buildName = build;
      }

    public String getBuildName()
      {
      return this.buildName;
      }

    public void addChromosome(Chromosome chr)
      {
      this.Chromosomes.put(chr.getName(), chr);
      }

    public void addChromosomes(Chromosome[] chromosomes)
      {
      for (Chromosome chromosome: chromosomes)
        this.addChromosome(chromosome);
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

    public abstract Genome mutate(int window);
    /* TODO Not really sure how this should work in the actual code yet. */
    // one thing, each chromosome can be mutated concurrently in the reference genome as theere will
    // not be any translocations.  I don't actually think I know how to do that...
    //public abstract void mutateChromosome(String chr, Location loc);
    }
