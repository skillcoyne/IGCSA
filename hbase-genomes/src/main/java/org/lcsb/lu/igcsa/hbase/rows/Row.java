/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.Column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Row
  {
  static Logger log = Logger.getLogger(Row.class.getName());

  private String rowId;

  private List<Column> columns = new ArrayList<Column>();

  public Row(String rowId)
    {
    this.rowId = rowId;
    }

  public Row(String rowId, Column[] columns)
    {
    this.rowId = rowId;
    this.columns = Arrays.asList(columns);
    }

  public void addColumn(Column column)
    {
    columns.add(column);
    }

  public void setColumns(Column... columns)
    {
    this.columns = Arrays.asList(columns);
    }

  public byte[] getRowId()
    {
    return Bytes.toBytes(rowId);
    }

  public String getRowIdAsString()
    {
    return rowId;
    }

  public List<Column> getColumns()
    {
    return this.columns;
    }


  public abstract boolean isRowIdCorrect();


  }
