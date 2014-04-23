/**
 * org.lcsb.lu.igcsa.generator
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.generator;

public class Aneuploidy
  {
  private String chr;
  private int gain = 0;
  private int loss = 0;

  private int maxGain = 6;
  private int maxLoss = 2;

  public Aneuploidy(String chr)
    {
    this.chr = chr;
    }

  public Aneuploidy(String chr, int count)
    {
    this.chr = chr;
    if (count > 0)
      this.gain += count;
    else
      this.loss += count;
    }

  public void gain(int count)
    {
    this.gain += count;
    if (gain > maxGain)
      gain = maxGain;
    }

  public void lose(int count)
    {
    this.loss += count;
    if (loss > maxLoss)
      loss = maxLoss;
    }

  public String getChromosome()
    {
    return chr;
    }

  public int getGain()
    {
    return gain;
    }

  public int getLoss()
    {
    return loss;
    }

  public int getCount()
    {
    return gain - loss;
    }

//    public int getCount()
//      {
//      return count;
//      }

//    public boolean isGain()
//      {
//      return (count > 0) ? true : false;
//      }

  @Override
  public boolean equals(Object o)
    {
    Aneuploidy obj = (Aneuploidy) o;
    return (obj.getChromosome().equals(this.getChromosome())) ? true : false;
    }

  @Override
  public String toString()
    {
    return this.getChromosome() + "(+" + gain + ", -" + loss + ")";
    }
  }
