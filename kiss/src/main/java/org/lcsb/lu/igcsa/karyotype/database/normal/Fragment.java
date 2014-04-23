package org.lcsb.lu.igcsa.karyotype.database.normal;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Fragment
  {
  private String chr;
  private int binId;
  private int count;
  private String variation;

  public String getVariation()
    {
    return variation;
    }

  public void setVariation(String variation)
    {
    this.variation = variation;
    }

  public int getCount()
    {
    return count;
    }

  public void setCount(int count)
    {
    this.count = count;
    }

  public String getChr()
    {
    return chr;
    }

  public void setChr(String chr)
    {
    this.chr = chr;
    }

  public int getBinId()
    {
    return binId;
    }

  public void setBinId(int binId)
    {
    this.binId = binId;
    }

  public String toString()
    {
    String str = "Chr " + chr + ", bin " + binId + " variation " + variation + " count " + count;
    return super.toString() + ": " + str;
    }
  }
