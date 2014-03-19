/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;


public class ChromosomeResult extends AbstractResult
  {
  private long length;
  private long segmentNumber;
  private String chrName;
  private String genomeName;


  protected ChromosomeResult(byte[] rowId)
    {
    super(rowId);
    }

  protected ChromosomeResult(String rowId)
    {
    super(rowId);
    }

  public long getLength()
    {
    return length;
    }

  public void setLength(byte[] length)
    {
    argTest(length);
    this.length = Bytes.toLong(length);
    }

  public long getSegmentNumber()
    {
    return segmentNumber;
    }

  public void setSegmentNumber(byte[] segmentNumber)
    {
    argTest(segmentNumber);
    this.segmentNumber = Bytes.toLong(segmentNumber);
    }

  public String getChrName()
    {
    return chrName;
    }

  public void setChrName(byte[] chrName)
    {
    argTest(chrName);
    this.chrName = Bytes.toString(chrName);
    }

  public String getGenomeName()
    {
    return genomeName;
    }

  public void setGenomeName(byte[] genomeName)
    {
    argTest(genomeName);
    this.genomeName = Bytes.toString(genomeName);
    }


  }
