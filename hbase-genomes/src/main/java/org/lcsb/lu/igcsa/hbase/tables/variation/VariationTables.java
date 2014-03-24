package org.lcsb.lu.igcsa.hbase.tables.variation;

import org.lcsb.lu.igcsa.hbase.tables.TableDefinitions;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.hbase.tables.variation
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public enum VariationTables implements TableDefinitions
  {
    VPB("variation_per_bin", 60)
        {
        @Override
        public Map<String, Set<String>> getRequiredFamilies()
          {
          Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
          reqFields.put("chr", new HashSet<String>(Arrays.asList("name")));
          reqFields.put("var", new HashSet<String>(Arrays.asList("name", "count", "class")));
          reqFields.put("gc", new HashSet<String>(Arrays.asList("min", "max", "frag")));
          return reqFields;
          }
        },
    SNVP("snv_probability", 1)
        { // RowIDS:  A-C, A-T etc...
        @Override
        public Map<String, Set<String>> getRequiredFamilies()
          {
          Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
          reqFields.put("prob", new HashSet<String>(Arrays.asList("val")));
          return reqFields;
          }
        },
    SIZE("variation_size_probability", 1)
        { // RowIDS: SNV_10, SNV_100, etc
        @Override
        public Map<String, Set<String>> getRequiredFamilies()
          {
          Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
          reqFields.put("var", new HashSet<String>(Arrays.asList("name")));
          reqFields.put("bp", new HashSet<String>(Arrays.asList("max", "prob")));
          return reqFields;
          }
        },
    GC("gc_bin", 1)
        { // RowIDS: X:0-85
        @Override
        public Map<String, Set<String>> getRequiredFamilies()
          {
          Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
          reqFields.put("chr", new HashSet<String>(Arrays.asList("name")));
          reqFields.put("gc", new HashSet<String>(Arrays.asList("min", "max")));
          reqFields.put("frag",new HashSet<String>(Arrays.asList("total")));
          return reqFields;
          }
        };


  private String tableName;
  private int splits;
  private VariationTables(String tn, int s)
    {
    tableName = tn;
    splits = s;
    }

  public String getTableName()
    {
    return tableName;
    }

  @Override
  public int regionSplits()
    {
    return splits;
    }


  public static VariationTables valueOfName(String tn)
    {
    for (VariationTables t: VariationTables.values())
      if (t.getTableName().equals(tn)) return t;
    return null;
    }


  public static String[] getTableNames()
    {
    List<String> tables = new ArrayList<String>();
    for (VariationTables t : VariationTables.values())
      tables.add(t.getTableName());

    return tables.toArray(new String[tables.size()]);
    }

  public abstract Map<String, Set<String>> getRequiredFamilies();
  }
