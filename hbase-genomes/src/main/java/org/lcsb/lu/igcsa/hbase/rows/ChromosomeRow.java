/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.Column;

public class ChromosomeRow extends Row
  {
  private String chr;
  private String genome;


  public static String createRowId(String genome, String chr)
    {
    return chr + "-" + genome;
    }

  public ChromosomeRow(String rowId)
    {
    super(rowId);
    }

  @Override
  public boolean isRowIdCorrect()
    {
    return ( genome == null || chr == null || !this.getRowIdAsString().equals(createRowId(genome, chr)) )? false: true;
    }

  public void addGenome(String name)
    {
    super.addColumn( new Column("info", "genome", name) );
    this.genome = name;
    }

  public void addChromosomeInfo(String chrName, long length, long numSegments)
    {
    super.addColumn( new Column("chr", "name", chrName) );
    super.addColumn( new Column("chr", "length", length) );
    super.addColumn( new Column("chr", "segments", numSegments) );

    this.chr = chrName;
    }


  }
