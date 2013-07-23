package org.lcsb.lu.igcsa.variation.fragment;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.prob.Probability;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import static org.lcsb.lu.igcsa.genome.Nucleotides.*;


/**
 * org.lcsb.lu.igcsa.variation
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SNV extends Variation
  {
  static Logger log = Logger.getLogger(SNV.class.getName());

  private Map<Character, Probability> snvFrequencies;

  public void setSnvFrequencies(Map<Character, Probability> frequencyMap)
    {
    this.snvFrequencies = frequencyMap;
    }

  public Map<Character, Probability> getSNVFrequencies()
    {
    return this.snvFrequencies;
    }

  /*
  Randomly selects nucleotides to attempt to mutate based on their frequency table.  Stops when the max (count)
  is reached.
  */
  public DNASequence mutateSequence(String sequence)
    {
    long start = System.currentTimeMillis();

    int count = this.fragment.getCount();
    log.debug(sequence.length() + " expected count " + count);

    lastMutations = new LinkedHashMap<Location, DNASequence>();

    char[] nucleotides = sequence.toCharArray();

    int totalSNPs = 0;
    // Just tracks the indecies that we've already attempted to mutate or mutated so that they are not mutated more than once.
    TreeSet<Integer> noReplacement = new TreeSet<Integer>();
    while (totalSNPs < count)
      {
      if (noReplacement.size() == sequence.length()) break; // this really only seems to happen in testing as I'm not using long sequences but...

      int nIndex = siteSelector.nextInt(sequence.length());

      while (noReplacement.contains(nIndex)) nIndex = siteSelector.nextInt(sequence.length());
      noReplacement.add(nIndex);

      char n = nucleotides[nIndex];
      if (n == GAP.value() || n == UNKNOWN.value()) continue;

      Probability f = snvFrequencies.get(n);
      char newN = (Character) f.roll();
      if (newN != n)
        {
        nucleotides[nIndex] = newN;
        lastMutations.put( new Location(nIndex, nIndex), new DNASequence(String.valueOf(newN)) );
        ++totalSNPs;
        }
      }

    long elapsed = System.currentTimeMillis() - start;
    log.debug("Mutation took " + elapsed);

    return new DNASequence(String.valueOf(nucleotides));
    }

  }
