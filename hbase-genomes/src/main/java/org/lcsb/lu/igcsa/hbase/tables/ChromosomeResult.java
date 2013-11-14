/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class ChromosomeResult extends AbstractResult
  {
  static Logger log = Logger.getLogger(ChromosomeResult.class.getName());

  private int length;
  private int segmentNumber;
  private String chrName;
  private String genomeName;


  protected ChromosomeResult(byte[] rowId)
    {
    super(rowId);
    }

  public int getLength()
    {
    return length;
    }

  public void setLength(byte[] length)
    {
    this.length = Bytes.toInt(length);
    }

  public int getSegmentNumber()
    {
    return segmentNumber;
    }

  public void setSegmentNumber(byte[] segmentNumber)
    {
    this.segmentNumber = Bytes.toInt(segmentNumber);
    }

  public String getChrName()
    {
    return chrName;
    }

  public void setChrName(byte[] chrName)
    {
    this.chrName = Bytes.toString(chrName);
    }

  public String getGenomeName()
    {
    return genomeName;
    }

  public void setGenomeName(byte[] genomeName)
    {
    this.genomeName = Bytes.toString(genomeName);
    }


  }
