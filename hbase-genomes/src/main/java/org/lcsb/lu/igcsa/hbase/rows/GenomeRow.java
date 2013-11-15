package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.commons.lang.StringUtils;
import org.lcsb.lu.igcsa.hbase.tables.Column;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class GenomeRow extends Row
  {

  public GenomeRow(String rowId)
    {
    super(rowId);
    super.addColumn( new Column("info", "name", rowId) );
    }

  @Override
  public boolean isRowIdCorrect()
    {
    return true; // always the case, see constructor
    }

  public void addParentColumn(String parentId)
    {
    if (parentId != null)
      super.addColumn( new Column("info", "parent", parentId) );
    }

  public void addChromosomeColumn(String... chrs)
    {
    super.addColumn( new Column("chr", "list", StringUtils.join(chrs, ",")) );
    }

  }
