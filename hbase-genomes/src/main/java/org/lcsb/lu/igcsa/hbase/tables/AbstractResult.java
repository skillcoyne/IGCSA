/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public abstract class AbstractResult
  {
  protected static final Log log = LogFactory.getLog(AbstractResult.class);

  protected String rowId;

  protected AbstractResult(byte[] rowId)
    {
    this.rowId = Bytes.toString(rowId);
    }

  protected AbstractResult(String rowId)
    {
    this.rowId = rowId;
    }

  protected AbstractResult()
    {
    }

  public String getRowId()
    {
    return rowId;
    }

  }
