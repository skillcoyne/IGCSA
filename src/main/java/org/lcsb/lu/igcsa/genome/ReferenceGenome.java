package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenome extends AbstractGenome
  {
  private String build;
  private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

  public ReferenceGenome()
    {
    super();
    }

  public ReferenceGenome(String build)
    {
    super(build);
    }

  @Override
  public Genome mutate(int window)
    {
    /* loop through the sequence of each chromosome, broken into window sizes and apply the variation objects. */
    Genome mutated = new ReferenceGenome();
    for (Chromosome chr: this.getChromosomes())
      {
      String sequence;
      while(true)
        {
        sequence = chr.getDNASequence(window);
        // mutate...how do I decide?

        if (sequence.length() < window) break;
        }
      }
    return mutated;
    }


  }



