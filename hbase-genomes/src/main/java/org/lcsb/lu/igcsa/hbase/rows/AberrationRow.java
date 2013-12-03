/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;

public class AberrationRow extends Row
  {
  private String karyotypeName;
  private Aberration aberration;


  public static String createRowId(String genome, String abr)
    {
    return genome + "-" + abr;
    }

  public AberrationRow(String rowId)
    {
    super(rowId);
    }

  public void addKaryotype(String karyotype)
    {
    this.karyotypeName = karyotype;
    this.addColumn(new Column("info", "karyotype", karyotypeName));
    }

  public void addAberration(Aberration abr) throws IOException
    {
    addColumn(new Column("abr", "type", abr.getAberration().getCytogeneticDesignation()));
    int i = 1;
    for (Band band: abr.getBands())
      {
      addColumn( new Column("abr", "chr"+i, band.getChromosomeName()) );
      addColumn( new Column("abr", "loc"+i, band.getLocation().getStart() + "-" + band.getLocation().getEnd()));
      ++i;
      }
    aberration = abr;
    }

  @Override
  public boolean isRowIdCorrect()
    {
    return (karyotypeName == null || aberration == null || !this.getRowIdAsString().equals(createRowId(karyotypeName, aberration.getWithLocations())))? false: true;
    }
  }
