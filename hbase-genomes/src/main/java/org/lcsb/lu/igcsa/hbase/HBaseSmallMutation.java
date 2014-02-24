/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;
import org.lcsb.lu.igcsa.hbase.tables.SmallMutationsResult;
import org.lcsb.lu.igcsa.hbase.tables.SmallMutationsTable;

import java.io.IOException;

public class HBaseSmallMutation extends HBaseConnectedObjects
  {
  private static final Log log = LogFactory.getLog(HBaseSmallMutation.class);

  private SmallMutationsTable smT;
  private SmallMutationsResult mutation;

  protected HBaseSmallMutation(AbstractResult result) throws IOException
    {
    super(result);
    this.mutation = (SmallMutationsResult) result;
    }

  protected HBaseSmallMutation(String rowId) throws IOException
    {
    super(rowId);
    this.mutation = this.smT.queryTable(rowId);
    }

  @Override
  public void closeTables() throws IOException
    {
    smT.close();
    }


  @Override
  protected void getTables() throws IOException
    {
    this.smT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSmallMutationsTable();
    }
  }
