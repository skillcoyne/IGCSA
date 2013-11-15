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

  private int length;
  private int segmentNumber;
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

  @Override
  public List<SequenceResult> getAssociatedResults(String rowId, AbstractTable connectedTable) throws IOException
    {
    SequenceTable seqTable = (SequenceTable) connectedTable;

    List<SequenceResult> sequences = new ArrayList<SequenceResult>();
    for (int i = 1; i <= this.getSegmentNumber(); i++)
      sequences.add(seqTable.queryTable(SequenceRow.createRowId(genomeName, chrName, i)));

    return sequences;
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
    if (rowId == null)
      rowId = ChromosomeRow.createRowId(genomeName, this.chrName);
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
