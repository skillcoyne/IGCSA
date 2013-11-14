package org.lcsb.lu.igcsa.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class GenomeTableAdmin
  {
  private static Map<String, HTable> admin;

  public static HTable getHTable(Configuration conf, String tableName) throws IOException
    {
    if (admin == null)
      admin = new HashMap<String, HTable>();

    if (!admin.containsKey(tableName))
      admin.put(tableName, new HTable(conf, tableName));

    return admin.get(tableName);
    }

  public static HTable getHTable(String tableName)
    {
    return admin.get(tableName);
    }

  public static Set<String> getTableNames()
    {
    if (admin != null)
      return admin.keySet();
    else
      return new HashSet<String>();
    }



  }
