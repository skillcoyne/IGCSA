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

public class ChromosomeResult extends AbstractResult
  {
  static Logger log = Logger.getLogger(ChromosomeResult.class.getName());

  private int length;
  private int segmentNumber;
  private String chrName;
  private String genomeName;


  public ChromosomeResult(byte[] rowId)
    {
    super(rowId);
    }

  public ChromosomeResult(String rowId)
    {
    super(rowId);
    }


  public void setLength(int length)
    {
    this.length = length;
    }

  public void setSegmentNumber(int segmentNumber)
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
