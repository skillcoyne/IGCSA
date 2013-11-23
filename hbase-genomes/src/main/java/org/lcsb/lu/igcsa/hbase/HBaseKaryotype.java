/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;
import org.lcsb.lu.igcsa.hbase.tables.KaryotypeAberrationTable;
import org.lcsb.lu.igcsa.hbase.tables.KaryotypeIndexTable;

import java.io.IOException;

public class HBaseKaryotype extends HBaseConnectedObjects
  {
  static Logger log = Logger.getLogger(HBaseKaryotype.class.getName());

  private KaryotypeAberrationTable kT;
  private KaryotypeIndexTable kiT;

  private KaryotypeIndexTable.KaryotypeIndexResult karyotype;
  //private List<>

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


  private void getAberrations()
    {
    //AberrationResult r = this.kT.getRows();
    }


  @Override
  protected void getTables() throws IOException
    {
    this.kiT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getKaryotypeIndexTable();
    this.kT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getKaryotypeTable();
    }
  }
