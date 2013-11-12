package org.lcsb.lu.igcsa.hbase;

import org.apache.hadoop.hbase.util.Bytes;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class GenomeColumnFamily
  {

  public static enum Families
    { d, seq }

  private static Set<String> gCols;

  static
    {
    gCols = new HashSet<String>();
    gCols.add("name");
    gCols.add("parent");
    }

  private static Set<String> seqCols;

  static
    {
    seqCols = new HashSet<String>();
    seqCols.add("start");
    seqCols.add("end");
    seqCols.add("sequence");
    }

  private static Map<Families, Set<String>> colFamily;


  private static GenomeColumnFamily columnFamily;

  public static GenomeColumnFamily getColumnFamilies()
    {
    if (columnFamily == null)
      columnFamily = new GenomeColumnFamily();

    return columnFamily;
    }

  private GenomeColumnFamily()
    {
    colFamily = new EnumMap<Families, Set<String>>(Families.class);
    colFamily.put(Families.d, gCols);
    colFamily.put(Families.seq, seqCols);
    }

  public byte[] getFamily(Families f)
    {
    return Bytes.toBytes(f.toString());
    }

  public Set<Families> getFamilies()
    {
    return colFamily.keySet();
    }

  public Set<String> getColumns(Families f)
    {
    return columnFamily.getColumns(f);
    }

  public Set<String> getColumns(String fs)
    {
    return getColumns(Families.valueOf(fs));
    }


  }
