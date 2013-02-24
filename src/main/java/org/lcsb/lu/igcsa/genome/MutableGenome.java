package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.variation.SNP;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class MutableGenome implements Genome
  {
  protected String buildName;
  protected HashMap<String, Chromosome> chromosomes = new HashMap<String, Chromosome>();
  private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

  public MutableGenome()
    {
    }

  public MutableGenome(String build)
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
    for (Chromosome chromosome : chromosomes)
      this.addChromosome(chromosome);
    }

  public boolean hasChromosome(String name)
    {
    return chromosomes.containsKey(name);
    }

  public Chromosome[] getChromosomes()
    {
    return chromosomes.values().toArray(new Chromosome[chromosomes.size()]);
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
    for (Iterator it = list.listIterator(); it.hasNext(); )
      {
      Map.Entry<Variation, ProbabilityList> entry = (Map.Entry) it.next();
      sortedMap.put(entry.getKey(), entry.getValue());
      }
    this.variationProbabilities = sortedMap;
    return this.variationProbabilities;
    }

  /*
    Not recommended for general use unless you have very small chromosomes as this holds the entire
    genome in memory. Better use is to call #mutate(Chromosome chr, int window) and output the new
    chromosome.
   */
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
    DNASequence currentSequence = chr.readSequence(window);
    System.out.println(currentSequence.getSequence());
    for (Iterator<Variation> it = this.getVariations().keySet().iterator(); it.hasNext(); )
      {
      try
        {   // This is so wrong!  It's going to potentially mutate the same sequence several times..
        Variation var = it.next();
        var.setProbabilityList(this.getVariations().get(var));
        DNASequence newSequence = var.mutateSequence(currentSequence);
        /* if the sequence mutated add to chromosome with the location
        otherwise don't so the entire sequence is not in memory*/
        if (newSequence != null) chr.alterSequence(currentSequence.getLocation(), newSequence);
        }
      catch (ProbabilityException e)
        { e.printStackTrace(); }
      }
    return chr;
    }

  /*
 This is so wrong!  It's going to potentially mutate the same sequence several times..
  */
  public void mutate(Chromosome c, int win, FASTAWriter w) throws IOException
    {
    final FASTAWriter writer = w;
    final int window = win;
    final Chromosome chr = c;

    System.out.println(chr.getName());
    String currentSequenceFragment;
    //DNASequence chromosomeSeq = new DNASequence();
    Location location = new Location(0, window); // FASTA locations are 0 based.
    while ((currentSequenceFragment = chr.getSequence(location)) != null)
      {
      location = new Location(location.getStart() + window, location.getEnd() + window);
      //      for (Iterator<Variation> it = this.getVariations().keySet().iterator(); it.hasNext(); )
      //        {
      //        try
      //          {
      //          Variation var = it.next();
      //          ProbabilityList pl = this.getVariations().get(var);
      //          var.setProbabilityList(pl);
      //          DNASequence sequence = var.mutateSequence(new DNASequence(currentSequenceFragment));
      //          writer.writeLine(sequence.getSequence());
      //          }
      //        catch (ProbabilityException e)
      //          {
      //          e.printStackTrace();
      //          }
      //        }
      try
        {
        System.out.println(location.getStart() + "-" + location.getEnd() + " " + currentSequenceFragment);
        writer.writeLine(currentSequenceFragment);
        }
      catch (IOException e)
        {
        e.printStackTrace();
        }
      if (currentSequenceFragment.length() < window) break;
      }
    writer.flush();
    }

  }
