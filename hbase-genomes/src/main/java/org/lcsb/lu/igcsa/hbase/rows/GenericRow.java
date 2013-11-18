/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;

public class GenericRow extends Row
  {
  static Logger log = Logger.getLogger(GenericRow.class.getName());

  public GenericRow(String rowId)
    {
    super(rowId);
    }



  @Override
  public boolean isRowIdCorrect()
    {
    return true;
    }
  }
