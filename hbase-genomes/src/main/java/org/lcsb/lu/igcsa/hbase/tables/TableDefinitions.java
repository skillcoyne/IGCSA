package org.lcsb.lu.igcsa.hbase.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public interface TableDefinitions
  {
  public String getTableName();

  public int regionSplits();

  public byte[] getStartKey();

  public byte[] getEndKey();

  public abstract Map<String, Set<String>> getRequiredFamilies();
  }
