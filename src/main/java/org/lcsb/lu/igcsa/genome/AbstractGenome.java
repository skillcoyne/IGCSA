package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.SNP;
import org.lcsb.lu.igcsa.variation.Variation;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

// TODO AbstractGenome may not be a necessary abstraction since I'm thus far doing nothing special with the reference vs cancer
public abstract class AbstractGenome implements Genome
    {
    protected String buildName;
    protected HashMap<String, Chromosome> chromosomes = new HashMap<String, Chromosome>();
    private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

    public AbstractGenome()
      {}

    public AbstractGenome(String build)
      {
      this.buildName = build;
      }

    public String getBuildName()
      {
      return this.buildName;
      }

    public void addChromosome(Chromosome chr)
      {
      this.chromosomes.put(chr.getName(), chr);
      }

    public void addChromosomes(Chromosome[] chromosomes)
      {
      for (Chromosome chromosome: chromosomes)
        this.addChromosome(chromosome);
      }

    public boolean hasChromosome(String name)
      {
      return chromosomes.containsKey(name);
      }

    public Chromosome[] getChromosomes()
      {
      return chromosomes.values().toArray( new Chromosome[chromosomes.size()]);
      }

    public void addVariationType(Variation v, ProbabilityList pl)
      {
      this.variationProbabilities.put(v, pl);
      }

    /*
    Sorted variation map by the ProbabilibyList frequencies.
     */
    public Map<Variation, ProbabilityList> getVariations()
      {
      List list = new LinkedList<Map.Entry<Variation, ProbabilityList>>(this.variationProbabilities.entrySet());
      Collections.sort(list, new Comparator<Map.Entry<Variation, ProbabilityList>>()
        {
        public int compare(Map.Entry<Variation, ProbabilityList> me1, Map.Entry<Variation, ProbabilityList> me2)
          {
          return me1.getValue().compareTo(me2.getValue());
          }
        });

      Map<Variation, ProbabilityList> sortedMap = new LinkedHashMap<Variation, ProbabilityList>();
      for (Iterator it = list.listIterator(); it.hasNext();)
        {
        Map.Entry<Variation, ProbabilityList> entry = (Map.Entry) it.next();
        sortedMap.put(entry.getKey(), entry.getValue());
        }
      this.variationProbabilities = sortedMap;
      return this.variationProbabilities;
      }

    public void mutate(int window)
      {
      Map<Variation, ProbabilityList> variations = this.getVariations();
      String currentSequenceFragment;
      for (Chromosome chr : this.getChromosomes())
        {
        Chromosome mutatedChr = mutate(chr, window);
        // replace the chromosome -- might be better to just write it or else this could get much too large...
        this.chromosomes.put(chr.getName(), mutatedChr);
        }
      }

    /*
    This may not be the way to do it.  Basically there's a chance (based on the frequency of the variation) for each variation to apply
    to the given window. I'm not sure if this is correct.
     */
    public Chromosome mutate(Chromosome chr, int window)
      {
      String currentSequenceFragment;
      DNASequence chromosomeSeq = new DNASequence();
      while (true)
        {
        currentSequenceFragment = chr.getDNASequence(window);
        for (Iterator<Variation> it = this.getVariations().keySet().iterator(); it.hasNext();)
          {
          try
            {
            Variation var = it.next();
            ProbabilityList pl = this.getVariations().get(var);
            var.setProbabilityList(pl);
            DNASequence sequence = var.mutateSequence( new DNASequence(currentSequenceFragment) );
            chromosomeSeq.merge(sequence);
            }
          catch (ProbabilityException e)
            {
            e.printStackTrace();
            }
          }
        if (currentSequenceFragment.length() < window) break;
        }

      return new Chromosome(chr.getName(), chromosomeSeq );
      }
    }
