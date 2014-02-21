/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.dist;

import org.apache.commons.lang.math.Range;
import org.apache.log4j.Logger;

import java.util.Random;

public class RandomRange
  {
  static Logger log = Logger.getLogger(RandomRange.class.getName());

  private int min = 0;
  private int max = 1;

  private Random rand = new Random();

  public RandomRange(Range range)
    {
    this.min = range.getMinimumInteger();
    this.max = range.getMaximumInteger();
    }

  public RandomRange(int min, int max)
    {
    this.min = min;
    this.max = max;
    }

  public int nextInt()
    {
    return rand.nextInt(max - min+1) + min;
    }



  }
