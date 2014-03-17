/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.genomes.IGCSATables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class IGCSAHbaseAdmin
  {
  private static final Log log = LogFactory.getLog(IGCSAHbaseAdmin.class);

  protected Configuration conf;
  protected HBaseAdmin hbaseAdmin;

  protected IGCSAHbaseAdmin(Configuration conf) throws IOException
    {
    this.conf = conf;
    //this.conf.setInt("timeout", 10);
    this.hbaseAdmin = new HBaseAdmin(conf);

    int tryRunning = 1;
    while (!this.hbaseAdmin.isMasterRunning() && tryRunning < 20)
      {
      try
        {
        this.wait(10);
        log.warn("Waiting for master to run:" + this.hbaseAdmin.getClusterStatus().toString());
        }
      catch (InterruptedException e)
        {
        throw new RuntimeException(e);
        }
      ++tryRunning;
      }
    }

  public void closeConections() throws IOException
    {
    this.hbaseAdmin.close();
    }

  public boolean tableExists(String tableName) throws IOException
    {
    return this.hbaseAdmin.tableExists(tableName);
    }

  public void disableTables(String[] tableNames) throws IOException
    {
    for (String t : tableNames)
      if (hbaseAdmin.tableExists(t) && hbaseAdmin.isTableEnabled(t)) hbaseAdmin.disableTable(t);
    }

  public void deleteTables(String[] tableNames) throws IOException
    {
    disableTables(tableNames);
    for (String t : tableNames)
      {
      if (hbaseAdmin.tableExists(t))
        hbaseAdmin.deleteTable(t);
      }
    }

  public String[] listTables() throws IOException
    {
    HTableDescriptor[] tables = hbaseAdmin.listTables();
    List<String> tableNames = new ArrayList<String>();
    for (HTableDescriptor t: tables)
      tableNames.add(t.getNameAsString());

    return tableNames.toArray(new String[tableNames.size()]);
    }

  public abstract boolean tablesExist() throws IOException;
  public abstract void createTables() throws IOException;

  }
