package org.lcsb.lu.igcsa.genome.concurrency;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;

import java.util.concurrent.Callable;


/**
 * org.lcsb.lu.igcsa.genome
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class Mutable implements Callable<Chromosome>
  {
  protected Chromosome chromosome;

  protected FASTAWriter writer;
  protected MutationWriter mutationWriter;

  public void setWriters(FASTAWriter writer, MutationWriter mutationWriter)
    {
    this.writer = writer;
    this.mutationWriter = mutationWriter;
    }

  public MutationWriter getMutationWriter()
    {
    return mutationWriter;
    }

  public FASTAWriter getFASTAWriter()
    {
    return writer;
    }


  @Override
  public abstract Chromosome call() throws Exception;
  }
