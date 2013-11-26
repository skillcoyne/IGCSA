/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.AberrationRow;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;
import org.lcsb.lu.igcsa.hbase.tables.KaryotypeAberrationTable;
import org.lcsb.lu.igcsa.hbase.tables.KaryotypeIndexTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseKaryotype extends HBaseConnectedObjects
  {
  static Logger log = Logger.getLogger(HBaseKaryotype.class.getName());

  private KaryotypeAberrationTable kT;
  private KaryotypeIndexTable kiT;

  private KaryotypeIndexTable.KaryotypeIndexResult karyotype;
  private List<AberrationResult> aberrations;

  protected HBaseKaryotype(AbstractResult result) throws IOException
    {
    super(result);
    this.karyotype = (KaryotypeIndexTable.KaryotypeIndexResult) result;
    }

  protected HBaseKaryotype(String rowId) throws IOException
    {
    super(rowId);
    this.karyotype = this.kiT.queryTable(rowId);
    }

  public KaryotypeIndexTable.KaryotypeIndexResult getKaryotype()
    {
    return karyotype;
    }

  public List<AberrationResult> getAberrations() throws IOException
    {
    if (aberrations == null)
      {
      aberrations = new ArrayList<AberrationResult>();
      for (String abr : karyotype.getAberrations())
        aberrations.add(kT.queryTable(AberrationRow.createRowId(karyotype.getRowId(), abr)));
      }

    return aberrations;
    }


  @Override
  protected void getTables() throws IOException
    {
    this.kiT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getKaryotypeIndexTable();
    this.kT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getKaryotypeTable();
    }
  }
