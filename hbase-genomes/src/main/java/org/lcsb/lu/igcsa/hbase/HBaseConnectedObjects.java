/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;

public abstract class HBaseConnectedObjects
  {
  protected String rowId;

  protected HBaseConnectedObjects(AbstractResult result) throws IOException
    {
    getTables();
    this.rowId = result.getRowId();
    }

  protected HBaseConnectedObjects(String rowId) throws IOException
    {
    getTables();
    this.rowId = rowId;
    }


  protected HBaseConnectedObjects()
    {
    }


  protected abstract void getTables() throws IOException;
  }
