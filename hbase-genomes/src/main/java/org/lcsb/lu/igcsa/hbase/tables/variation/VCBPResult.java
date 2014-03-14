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
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;


public class VCBPResult extends AbstractResult
  {
  private static final Log log = LogFactory.getLog(VCBPResult.class);

  private String chr, variationName, variationClass;
  private long varCount, gcMin, gcMax;


  protected VCBPResult(byte[] rowId)
    {
    super(rowId);
    }

  protected VCBPResult(String rowId)
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

  public long getVariationCount()
    {
    return varCount;
    }

  public void setVarCount(byte[] varCount)
    {
    this.varCount = Bytes.toLong(varCount);
    }

  public long getGCMin()
    {
    return gcMin;
    }

  public void setGCMin(byte[] gcMin)
    {
    this.gcMin = Bytes.toLong(gcMin);
    }

  public long getGCMax()
    {
    return gcMax;
    }

  public void setGCMax(byte[] gcMax)
    {
    this.gcMax = Bytes.toLong(gcMax);
    }

  @Override
  public String toString()
    {
    return StringUtils.join( new String[]{chr, String.valueOf(varCount), variationName, String.valueOf(gcMin), String.valueOf(gcMax)}, ", ");
    }

  }
