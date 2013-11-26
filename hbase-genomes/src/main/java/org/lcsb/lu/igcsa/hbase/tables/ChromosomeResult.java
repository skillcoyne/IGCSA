/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChromosomeResult extends AbstractResult
  {
  static Logger log = Logger.getLogger(ChromosomeResult.class.getName());

  private long length;
  private long segmentNumber;
  private String chrName;
  private String genomeName;


  public ChromosomeResult(String chromosomeName, int length, int numSegments)
    {
    this.chrName = chromosomeName;
    this.length = length;
    this.segmentNumber = numSegments;
    }

  protected ChromosomeResult(byte[] rowId)
    {
    super(rowId);
    }

  protected ChromosomeResult(String rowId)
    {
    super(rowId);
    }

  public void setLength(long length)
    {
    this.length = length;
    }

  public void setSegmentNumber(long segmentNumber)
    {
    this.segmentNumber = segmentNumber;
    }

  public void setChrName(String chrName)
    {
    this.chrName = chrName;
    }

  public void setGenomeName(String genomeName)
    {
    this.genomeName = genomeName;
    if (rowId == null)
      rowId = ChromosomeRow.createRowId(genomeName, this.chrName);
    }

  public long getLength()
    {
    return length;
    }

  public void setLength(byte[] length)
    {
    this.length = Bytes.toLong(length);
    }

  public long getSegmentNumber()
    {
    return segmentNumber;
    }

  public void setSegmentNumber(byte[] segmentNumber)
    {
    this.segmentNumber = Bytes.toLong(segmentNumber);
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
