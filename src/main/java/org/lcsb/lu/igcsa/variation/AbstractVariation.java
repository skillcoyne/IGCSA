package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.fasta.NucleotideCodes;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;

import static org.lcsb.lu.igcsa.fasta.NucleotideCodes.*;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class AbstractVariation implements Variation
  {
  protected Probability probability;
  protected ProbabilityList probabilityList;

  protected static final char GAP = NucleotideCodes.GAP.getNucleotide();
  protected static final char UNKNOWN = NucleotideCodes.N.getNucleotide();


  protected AbstractVariation()
    {
    super();
    }

  protected AbstractVariation(Probability p) throws ProbabilityException
    {
    this.probability = p;
    }

  public void setProbability(Probability probability) throws ProbabilityException
    {
    this.probability = probability;
    }

  @Override
  public void setProbabilityList(ProbabilityList pl) throws ProbabilityException
    {
    this.probabilityList = pl;
    }

  public Probability getProbability()
    {
    return this.probability;
    }

  public DNASequence mutateSequence(String s)
    {
    DNASequence ds = new DNASequence(s);
    return this.mutateSequence(ds);
    }

  /**
   * Masks unknown sequences or gaps from the mutation
   * @param sequence
   * @return
   */
//  protected boolean isSequenceValid(DNASequence sequence)
//    { // TODO
//    return true;
//    }

  public abstract DNASequence mutateSequence(DNASequence sequence);
  }
