/**
 * org.lcsb.lu.igcsa.ea
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.ea;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.log4j.Logger;

public class Individual extends Chromosome
  {
  static Logger log = Logger.getLogger(Individual.class.getName());

  @Override
  public double fitness()
    {
    return 0;
    }
  }
