package org.lcsb.lu.igcsa.variation;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.lcsb.lu.igcsa.fasta.NucleotideCodes;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import sun.plugin2.message.GetAppletMessage;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SNP extends AbstractVariation
  {
  private ProbabilityList probabilityList;
  //private PoissonDistribution poissonDistribution;
  private DNASequence snpSeq;


  public SNP()
    {
    super();
    }

  public SNP(Probability p) throws ProbabilityException
    {
    super(p);
    //this.poissonDistribution = new PoissonDistribution(this.probability.getProbability());
    }

  public SNP(DNASequence s, Probability p) throws ProbabilityException
    {
    this(p);
    this.snpSeq = s; // not sure this is a good idea
    }

  public SNP(DNASequence s)
    {
    this.snpSeq = s;
    }

  @Override
  public void setProbability(Probability p) throws ProbabilityException
    {
    super.setProbability(p);
    if (p.getName().length() > 1) throw new ProbabilityException("SNP probability name should be a nucleotide");
    //this.poissonDistribution = new PoissonDistribution(this.probability.getFrequency()); // no idea if this is the right thing
    }

  @Override
  public void setProbabilityList(ProbabilityList pl) throws ProbabilityException
    {
    this.probabilityList = pl;
    //this.poissonDistribution = new PoissonDistribution(this.probabilityList.getFrequency());
    }

  public DNASequence mutateSequence(DNASequence sequence)
    {
    boolean mutated = false;
    //if (this.poissonDistribution == null) throw new IllegalStateException("Probabilities have not been set, cannot mutate sequence.");

    char[] nucleotides = sequence.getSequence().toCharArray();
    for (int i = 0; i < nucleotides.length; i++)
      {
      if (nucleotides[i] == GAP || nucleotides[i] == UNKNOWN) continue;
      //if (this.poissonDistribution.sample() > 0)
        {
        char snp = alterNucleotide(nucleotides[i]);
        if (snp == nucleotides[i]) mutated = true;
        nucleotides[i] = snp;
        }
      }
    return (mutated)? ( new DNASequence(new String(nucleotides)) ): null;
    }


  private char alterNucleotide(char n)
    {
    char finalNucleotide = n;
    for (Probability p : this.probabilityList.toArray())
      {
      // local probability for SNP?
      if (p.getProbability() <= 0) continue;
      PoissonDistribution nucleotidePD = new PoissonDistribution(p.getProbability());
      if (nucleotidePD.sample() > 0)
        {
        finalNucleotide = p.getName().toCharArray()[0];
        break; // don't continue trying to mutate if it already mutated
        }
      }
    return finalNucleotide;
    }

  }
