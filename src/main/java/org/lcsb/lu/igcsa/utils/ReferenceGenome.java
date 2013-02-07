package org.lcsb.lu.igcsa.utils;

import org.lcsb.lu.igcsa.genome.Chromosome;

import java.util.Collection;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenome
  {
  private String build;
  private Collection<Chromosome> chromosomes;

  public ReferenceGenome(String buildName, String fastaDir)
    {
    this.build = buildName;
    // TODO read fasta files build chromosome
    }
  }
