package org.lcsb.lu.igcsa.database.insilico;

import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.database.insilico
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface MutationDAO
  {

  public int insertMutation(Mutation mutation);

  public Mutation[] getMutationsByGenome(Genome genome);

  public Mutation[] getMutationsByChromosome(Genome genome, String chromosome);

  public Mutation[] getMutationsByFragment(Genome genome, String chromosome, int fragment);

  }
