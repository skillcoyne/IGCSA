/**
 * org.lcsb.lu.igcsa.hbase.tables.variation
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.variation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;


public class VCPBResult extends AbstractResult
  {
  private static final Log log = LogFactory.getLog(VCPBResult.class);

  private String chr, variationName, variationClass;
  private int varCount, gcMin, gcMax, fragCount;


  protected VCPBResult(byte[] rowId)
    {
    super(rowId);
    }

  protected VCPBResult(String rowId)
    {
    super(rowId);
    }

  public String getChromosome()
    {
    return chr;
    }

  public void setChromosome(byte[] chr)
    {
    this.chr = Bytes.toString(chr);
    }

  public String getVariationName()
    {
    return variationName;
    }

  public void setVariationName(byte[] variationName)
    {
    this.variationName = Bytes.toString(variationName);
    }

  public String getVariationClass()
    {
    return variationClass;
    }

  public void setVariationClass(byte[] variationClass)
    {
    this.variationClass = Bytes.toString(variationClass);
    }

  public int getFragmentNum()
    {
    return fragCount;
    }

  public void setFragmentNum(byte[] fragCount)
    {
    this.fragCount = Bytes.toInt(fragCount);
    }

  public int getVariationCount()
    {
    return varCount;
    }

  public void setVarCount(byte[] varCount)
    {
    this.varCount = Bytes.toInt(varCount);
    }

  public int getGCMin()
    {
    return gcMin;
    }

  public void setGCMin(byte[] gcMin)
    {
    this.gcMin = Bytes.toInt(gcMin);
    }

  public int getGCMax()
    {
    return gcMax;
    }

  public void setGCMax(byte[] gcMax)
    {
    this.gcMax = Bytes.toInt(gcMax);
    }

  @Override
  public String toString()
    {
    return StringUtils.join( new String[]{chr, "frag:"+String.valueOf(fragCount), "var:"+String.valueOf(varCount), variationName, String.valueOf(gcMin) + "-" + String.valueOf(gcMax)}, ", ");
    }

  }
