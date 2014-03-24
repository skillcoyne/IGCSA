package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.RegionSplitter;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.genomes.IGCSATables;
import org.lcsb.lu.igcsa.hbase.tables.genomes.SequenceTable;
import org.lcsb.lu.igcsa.hbase.tables.variation.*;

import java.io.IOException;

import static org.lcsb.lu.igcsa.hbase.tables.variation.VariationTables.*;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class VariationAdmin extends IGCSAHbaseAdmin
  {
  private static final Log log = LogFactory.getLog(VariationAdmin.class);

  private static VariationAdmin adminInstance;

  public static VariationAdmin getInstance() throws IOException
    {
    //if (adminInstance == null)
    adminInstance = new VariationAdmin(HBaseConfiguration.create());
    return adminInstance;
    }

  public static VariationAdmin getInstance(Configuration conf) throws IOException
    {
    adminInstance = new VariationAdmin(conf);
    return adminInstance;
    }

  protected VariationAdmin(Configuration conf) throws IOException
    {
    super(conf);
    }

  public AbstractTable<? extends AbstractTable> getTable(String tableName) throws IOException
    {
    switch (VariationTables.valueOfName(tableName))
      {
      case VPB:
        return new VariationCountPerBin(conf, VPB.getTableName());
      case SIZE:
        return new SizeProbability(conf, SIZE.getTableName());
      case SNVP:
        return new SNVProbability(conf, SNVP.getTableName());
      case GC:
        return new GCBin(conf, GC.getTableName());
      }
    return null;
    }

  public void disableTables() throws IOException
    {
    super.disableTables(VariationTables.getTableNames());
    }

  public void deleteTables() throws IOException
    {
    super.deleteTables(VariationTables.getTableNames());
    }

  @Override
  public void createTables() throws IOException, ParseException, InterruptedException
    {
    for (VariationTables table : VariationTables.values())
      {
      if (!hbaseAdmin.tableExists(table.getTableName()))
        {
        if (table.regionSplits() > 1 && hbaseAdmin.getConfiguration().getInt("mapred.map.tasks", 2) > 2)
          {
          RegionSplitter.main(new String[]{table.getTableName(), "-c", String.valueOf(table.regionSplits()),
              "-f", StringUtils.join(table.getRequiredFamilies().keySet().iterator(), ":")});
          }
        else
          hbaseAdmin.createTable(AbstractTable.getDescriptor(table));
        }
      }
    }

  @Override
  public boolean tablesExist() throws IOException
    {
    for (VariationTables table : VariationTables.values())
      {
      boolean exists = this.tableExists(table.getTableName());
      if (!exists) return false;
      }
    return true;
    }

  }
