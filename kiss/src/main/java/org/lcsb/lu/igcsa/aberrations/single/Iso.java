package org.lcsb.lu.igcsa.aberrations.single;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;


/**
 * org.lcsb.lu.igcsa.aberrations.single
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Iso extends SingleChromosomeAberration
  {
  static Logger log = Logger.getLogger(Iso.class.getName());

  @Override
  public void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter)
    {
    log.warn("Not yet impelmented");
    }
  }
