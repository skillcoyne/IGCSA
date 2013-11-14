/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.Column;

public class SequenceRow extends Row
  {
  static Logger log = Logger.getLogger(SequenceRow.class.getName());

  private String genome;
  private String chr;
  private int segment = 0;

  public static String createRowId(String genome, String chr, int segNum)
    {
    return ChromosomeRow.createRowId(genome, chr) + ":" + segNum;
    }

  public SequenceRow(String rowId)
    {
    super(rowId);
    }

  public void addBasePairs(String sequence)
    {
    super.addColumn( new Column("bp", "seq", sequence));
    }

  public void addLocation(String chr, int segmentNumber, int start, int end)
    {
    super.addColumn( new Column("loc", "chr", chr) );
    super.addColumn( new Column("loc", "start", start) );
    super.addColumn( new Column("loc", "end", end) );
    super.addColumn( new Column("loc", "segment", segmentNumber) );

    this.chr = chr;
    this.segment = segmentNumber;
    }

  public void addGenome(String name)
    {
    super.addColumn( new Column("info", "genome", name));
    this.genome = name;
    }

  @Override
  public boolean isRowIdCorrect()
    {
    return (chr == null || segment <= 0 || genome == null || ( !this.getRowIdAsString().equals(createRowId(genome, chr, segment))) )? false: true;
    }

  }
