package org.lcsb.lu.igcsa.karyotype.database.normal;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.Range;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Bin
  {
  private String chr;
  private int min;
  private int max;
  private int size;
  private int binId;

  public String getChr()
    {
    return chr;
    }

  public void setChr(String chr)
    {
    this.chr = chr;
    }

  public int getMin()
    {
    return min;
    }

  public void setMin(int min)
    {
    this.min = min;
    }

  public int getMax()
    {
    return max;
    }

  public void setMax(int max)
    {
    this.max = max;
    }

  public int getSize()
    {
    return size;
    }

  public void setSize(int size)
    {
    this.size = size;
    }

  public int getBinId()
    {
    return binId;
    }

  public void setBinId(int bin_id)
    {
    this.binId = bin_id;
    }

  public Range getRange()
    {
    return new IntRange(this.min, this.max);
    }

  }
