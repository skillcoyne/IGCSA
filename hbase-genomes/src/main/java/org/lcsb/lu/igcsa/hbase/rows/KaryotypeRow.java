/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;
import java.util.List;

public class KaryotypeRow extends Row
  {
  static Logger log = Logger.getLogger(KaryotypeRow.class.getName());

  private String genome;
  private String aberration;


  public static String createRowId(String genome, String abr)
    {
    return genome + "-" + abr;
    }

  public KaryotypeRow(String rowId)
    {
    super(rowId);
    }

  public void addGenome(String genome)
    {
    this.addColumn(new Column("info", "genome", genome));
    this.genome = genome;
    }

  public void addAberration(String type, List<String[]> abr) throws IOException
    {
    aberration = "";

    this.addColumn( new Column("abr", "type", type) );
    for (int i=0; i<abr.size(); i++)
      {
      String[] a = abr.get(i);

      if (a.length != 2)
        throw new IOException("Aberrations must come in pairs [chr, loc-loc]");

      this.addColumn( new Column("abr", "chr"+(i+1), a[0]) );
      this.addColumn( new Column("abr", "chr"+(i+1), a[1]) );

      aberration += StringUtils.join(a, ":") + ",";
      }

    aberration = StringUtils.removeEnd(aberration, ",");
    }


  @Override
  public boolean isRowIdCorrect()
    {
    return (genome == null || aberration == null || !this.getRowIdAsString().equals(createRowId(genome, aberration)))? false: true;
    }
  }
