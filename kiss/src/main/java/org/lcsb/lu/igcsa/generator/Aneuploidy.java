/**
 * org.lcsb.lu.igcsa.generator
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.log4j.Logger;

public class Aneuploidy
  {
  private String chr;
  private int gain = 0;
  private int loss = 0;

  private int max = 6;
  private int min = -2;

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
    }

  public void lose(int count)
    {
    this.loss += count;
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
