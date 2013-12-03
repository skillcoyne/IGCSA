/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.Column;

public class SequenceRow extends Row
  {
  static Logger log = Logger.getLogger(SequenceRow.class.getName());

  private String genome;
  private String chr;
  private long segmentNum;

  public static String createRowId(String genome, String chr, long segmentNum)
    {
    //1000000000

    String formattedSeg = String.format("%08d", segmentNum);
    return ChromosomeRow.createRowId(genome, chr) + ":" + formattedSeg;
    }

  public SequenceRow(String rowId)
    {
    super(rowId);
    }

  public void addBasePairs(String sequence)
    {
    super.addColumn( new Column("bp", "seq", sequence));
    }

  public void addLocation(String chr, long start, long end, long segmentNum)
    {
    super.addColumn( new Column("loc", "chr", chr) );
    super.addColumn( new Column("loc", "start", start) );
    super.addColumn( new Column("loc", "end", end) );
    super.addColumn( new Column("loc", "segment", segmentNum) );

    this.chr = chr;
    this.segmentNum = segmentNum;
    }

  public void addGenome(String name)
    {
    super.addColumn( new Column("info", "genome", name));
    this.genome = name;
    }

  @Override
  public boolean isRowIdCorrect()
    {
    if (chr == null || segmentNum <= 0 || genome == null )
      return false;
    return this.getRowIdAsString().equals(createRowId(genome, chr, segmentNum));
    }

  }
