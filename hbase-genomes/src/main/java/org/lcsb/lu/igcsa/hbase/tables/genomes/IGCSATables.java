package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.tables.TableDefinitions;

import java.util.*;


/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public enum IGCSATables  implements TableDefinitions
  {
    GN("genome", 1, new byte[0], new byte[0])
        {
        @Override
        public Map<String, Set<String>> getRequiredFamilies()
          {
          Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
          reqFields.put("info", new HashSet<String>(Arrays.asList("name")));
          reqFields.put("chr", new HashSet<String>(Arrays.asList("list")));
          return reqFields;
          }
        },
    CHR("chromosome", 1, new byte[0], new byte[0])
        {
        @Override
        public Map<String, Set<String>> getRequiredFamilies()
          {
          Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
          reqFields.put("info", new HashSet<String>(Arrays.asList("genome")));
          reqFields.put("chr", new HashSet<String>(Arrays.asList("length", "segments", "name")));
          return reqFields;
          }
        },
      SEQ("sequence", 60, Bytes.toBytes("AAAA00000000"), Bytes.toBytes("YZZZ99999999"))
          {
        @Override
        public Map<String, Set<String>> getRequiredFamilies()
          {
          Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
          reqFields.put("info", new HashSet<String>(Arrays.asList("genome")));
          reqFields.put("loc", new HashSet<String>(Arrays.asList("start", "end", "chr")));
          reqFields.put("bp", new HashSet<String>(Arrays.asList("seq")));
          return reqFields;
          }
        },
      SMUT("small_mutations", 60, Bytes.toBytes("AAAA00000000"), Bytes.toBytes("YZZZ99999999"))
          {
          @Override
          public Map<String, Set<String>> getRequiredFamilies()
            {
            Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
            reqFields.put("info", new HashSet<String>(Arrays.asList("genome", "mutation")));
            reqFields.put("loc", new HashSet<String>(Arrays.asList("segment", "chr", "start", "end")));
            reqFields.put("bp", new HashSet<String>(Arrays.asList("seq")));
            return reqFields;
            }
          },
      KI("karyotype_index", 1, new byte[0], new byte[0])
          {
          @Override
          public Map<String, Set<String>> getRequiredFamilies()
            {
            Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
            reqFields.put("info", new HashSet<String>(Arrays.asList("genome")));
            reqFields.put("abr", new HashSet<String>());
            reqFields.put("gain", new HashSet<String>());
            reqFields.put("loss", new HashSet<String>());
            return reqFields;
            }
          },
      KT("karyotype", 1, new byte[0], new byte[0])
          {
          @Override
          public Map<String, Set<String>> getRequiredFamilies()
            {
            Map<String, Set<String>> reqFields = new HashMap<String, Set<String>>();
            reqFields.put("info", new HashSet<String>(Arrays.asList("karyotype")));
            reqFields.put("abr", new HashSet<String>(Arrays.asList("type", "chr1", "loc1")));
            return reqFields;
            }
          };

  private String tableName;
  private int splits;
  private byte[] startKey, endKey;

  private IGCSATables(String tn, int s, byte[] startKey, byte[] endKey)
    {
    this.tableName = tn;
    this.splits = s;
    this.startKey = startKey;
    this.endKey = endKey;
    }

  @Override
  public String getTableName()
    {
    return tableName;
    }

  @Override
  public int regionSplits()
    {
    return splits;
    }

  @Override
  public byte[] getStartKey()
    {
    return startKey;
    }

  @Override
  public byte[] getEndKey()
    {
    return endKey;
    }


  public static String[] getTableNames()
    {
    List<String> tables = new ArrayList<String>();
    for (IGCSATables t: IGCSATables.values())
      tables.add(t.getTableName());

    return tables.toArray(new String[tables.size()]);
    }

  public abstract Map<String, Set<String>> getRequiredFamilies();
  }
