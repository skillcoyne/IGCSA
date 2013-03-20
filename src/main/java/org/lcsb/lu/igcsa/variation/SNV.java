package org.lcsb.lu.igcsa.variation;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.Nucleotides;
import org.lcsb.lu.igcsa.prob.Frequency;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.TreeSet;



/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SNV extends Variation
  {
  static Logger log = Logger.getLogger(SNV.class.getName());

  private Map<Character, Frequency> snvFrequencies;
  private LinkedHashMap<Location, DNASequence> lastMutations;

  private static final char GAP = Nucleotides.GAP.getNucleotide();
  private static final char UNKNOWN = Nucleotides.N.getNucleotide();

  public SNV(Map<Character, Frequency> frequencyMap)
    {
    this.snvFrequencies = frequencyMap;
    }

  /*
  Randomly selects nucleotides to attempt to mutate based on their frequency table.  Stops when the max (count)
  is reached.
  */
  public DNASequence mutateSequence(String sequence)
    {
    int count = this.fragment.getSNV();
    log.debug(sequence.length() + " expected count " + count);

    lastMutations = new LinkedHashMap<Location, DNASequence>();

    Random selector = new Random();
    char[] nucleotides = sequence.toCharArray();

    int totalSNPs = 0;
    // Just tracks the indecies that we've already attempted to mutate or mutated so that they are not mutated more than once.
    TreeSet<Integer> noReplacement = new TreeSet<Integer>();
    while (totalSNPs < count)
      {
      //log.debug("SNPs: " + totalSNPs);
      int nIndex = selector.nextInt(sequence.length());
      while (noReplacement.contains(nIndex)) nIndex = selector.nextInt(sequence.length());
      noReplacement.add(nIndex);

      char n = nucleotides[nIndex];
      if (n == GAP || n == UNKNOWN) continue;

      Frequency f = snvFrequencies.get(n);
      char newN = (Character) f.roll();
      if (newN != n)
        {
        nucleotides[nIndex] = newN;
        lastMutations.put( new Location(nIndex, nIndex), new DNASequence(String.valueOf(nucleotides)) );
        ++totalSNPs;
        }
      log.debug("Orig: " + n + " New: " + newN);
      }

    log.debug("Total SNPs:" + totalSNPs);
    return new DNASequence(String.valueOf(nucleotides));
    }

  /**
   * Note that these are not cumulative.  Each call to mutateSequence resets this map.
   * @return
   */
  public LinkedHashMap<Location, DNASequence> getLastMutations()
    {
    return this.lastMutations;
    }

  }
