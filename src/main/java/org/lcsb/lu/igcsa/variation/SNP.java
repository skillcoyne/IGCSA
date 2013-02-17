package org.lcsb.lu.igcsa.variation;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.Nucleotide;
import org.lcsb.lu.igcsa.prob.Probability;

import java.util.HashMap;
import java.util.Random;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SNP extends AbstractVariation
  {
  private PoissonDistribution poissonDistribution;
  private DNASequence snpSeq;
  private HashMap<Character, Nucleotide> nucleotideProbabilities = new HashMap<Character, Nucleotide>();

  public SNP()
    {
    super();
    }

  public SNP(Probability p)
    {
    super(p);
    this.poissonDistribution  = new PoissonDistribution( this.probability.getProbability() );
    }

  public SNP(DNASequence s)
    {
    super();
    this.snpSeq = s;
    }

  public SNP(DNASequence s, Probability p)
    {
    this(p);
    this.snpSeq = s;
    }

  public void setProbability(Probability p)
    {
    super.setProbability(p);
    this.poissonDistribution  = new PoissonDistribution( this.probability.getProbability() );
    }

  public DNASequence mutateSequence(DNASequence sequence)
    {
    if (this.probability == null) throw new IllegalStateException("Probability has not been set, cannot mutate sequence.");
    int s = this.poissonDistribution.sample();

    System.out.println(this.poissonDistribution.getMean());
    System.out.println(s);

    char[] nucleotides = sequence.getSequence().toCharArray();
    for(int i=0; i<nucleotides.length; i++)
      {
      if (this.poissonDistribution.sample() > 0) nucleotides[i] = alterNucleotide(nucleotides[i]);
      }

    System.out.println(sequence.getSequence() + " : " + new String(nucleotides) );

    return new DNASequence( new String(nucleotides) );
    }


  private char alterNucleotide(char n)
    {
    String nucleotides = "ACTG";

    Nucleotide nucProb = nucleotideProbabilities.get(n);

    for (Character c: nucProb.getProbabilities().keySet())
      {


      }


    switch (n) // TODO This is NOT correct.  There are further probabilities for a switch from one nucleotide to another that need to be applied
      {
      case 'A':
        return 'C';
      case 'C': return 'T';
      case 'T': return 'G';
      case 'G': return 'A';
      default:
        Random r = new Random(4);
        return nucleotides.charAt(r.nextInt());
      }
    }

  }
