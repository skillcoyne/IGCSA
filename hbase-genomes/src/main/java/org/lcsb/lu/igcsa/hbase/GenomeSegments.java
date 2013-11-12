/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenomeSegments
  {
  static Logger log = Logger.getLogger(GenomeSegments.class.getName());

  private Configuration conf;
  private HBaseAdmin admin;
  private HTable table;


  private final String tableName = "genome";


  public GenomeSegments() throws IOException, MasterNotRunningException
    {
    this(HBaseConfiguration.create());
    }

  public GenomeSegments(Configuration conf) throws IOException, MasterNotRunningException
    {
    this.conf = conf;
    conf.setInt("timeout", 10);
    this.admin = new HBaseAdmin(conf);
    init();
    }

  private void init() throws IOException
    {
    HTableDescriptor descriptor = new HTableDescriptor(tableName);

    for(GenomeColumnFamily.Families f: GenomeColumnFamily.getColumnFamilies().getFamilies())
      descriptor.addFamily(new HColumnDescriptor(f.toString()));

    if (!admin.tableExists(tableName))
      admin.createTable(descriptor);

    this.table = new HTable(conf, tableName);
    }


  public void addToRow()
    {
    GenomeRow row = new GenomeRow()
    }




  }
