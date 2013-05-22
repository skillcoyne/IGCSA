package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class DerivativeChromosome extends Chromosome
  {
  static Logger log = Logger.getLogger(DerivativeChromosome.class.getName());

  public DerivativeChromosome(String name)
    {
    super(name);
    }

  public DerivativeChromosome(String name, File chrFastaFile)
    {
    super(name, chrFastaFile);
    }





  }
