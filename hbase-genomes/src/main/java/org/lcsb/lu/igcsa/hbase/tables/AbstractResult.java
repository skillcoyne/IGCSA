/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public abstract class AbstractResult
  {
  static Logger log = Logger.getLogger(AbstractResult.class.getName());

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

  // this may break encapsulation but my class structure isn't very smart anyhow
  public List<? extends AbstractResult> getAssociatedResults(String rowId, AbstractTable connectedTable) throws IOException
    {
    return null;
    }

  }
