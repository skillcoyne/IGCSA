/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.tables.*;
import org.lcsb.lu.igcsa.hbase.tables.genomes.*;

import java.io.IOException;
import java.util.*;


public class HBaseGenomeAdmin extends IGCSAHbaseAdmin
  {
  private static final Log log = LogFactory.getLog(HBaseGenomeAdmin.class);

  private static HBaseGenomeAdmin adminInstance;

  public static HBaseGenomeAdmin getHBaseGenomeAdmin() throws IOException
    {
    //if (adminInstance == null)
      adminInstance = new HBaseGenomeAdmin(HBaseConfiguration.create());

    return adminInstance;
    }

  public static HBaseGenomeAdmin getHBaseGenomeAdmin(Configuration configuration) throws IOException
    {
    adminInstance = new HBaseGenomeAdmin(configuration);
    return adminInstance;
    }

  protected HBaseGenomeAdmin(Configuration conf) throws IOException
    {
    super(conf);
    }

  public GenomeTable getGenomeTable()
    {
    GenomeTable gt = null;
    try
      {
      gt = new GenomeTable(this.conf, IGCSATables.GN.getTableName());
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }
    return gt;
    }

  public ChromosomeTable getChromosomeTable()
    {
    ChromosomeTable ct = null;
    try
      {
      ct = new ChromosomeTable(conf, IGCSATables.CHR.getTableName());
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }
    return ct;
    }

  public SequenceTable getSequenceTable()
    {
    SequenceTable st = null;
    try
      {
      st = new SequenceTable(conf, IGCSATables.SEQ.getTableName());
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }
    return st;
    }

  public SmallMutationsTable getSmallMutationsTable()
    {
    SmallMutationsTable smt = null;
    try
      {
      smt = new SmallMutationsTable(conf, IGCSATables.SMUT.getTableName());
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }

    return smt;
    }

  public KaryotypeIndexTable getKaryotypeIndexTable()
    {
    KaryotypeIndexTable kiT = null;
    try
      {
      kiT = new KaryotypeIndexTable(conf, IGCSATables.KI.getTableName());
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }
    return kiT;
    }

  public KaryotypeAberrationTable getKaryotypeTable()
    {
    KaryotypeAberrationTable kT = null;
    try
      {
      kT = new KaryotypeAberrationTable(conf, IGCSATables.KT.getTableName());
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }
    return kT;
    }


//  public HBaseKaryotype getKaryotype(String karyotypeName) throws IOException
//    {
//    KaryotypeIndexTable kiT = this.getKaryotypeIndexTable();
//    KaryotypeIndexResult result = kiT.queryTable(karyotypeName);
//    return (result != null) ? new HBaseKaryotype(result) : null;
//    }

  public void deleteGenome(String genomeName) throws IOException
    {
    ChromosomeTable cT = this.getChromosomeTable();
    SequenceTable sT = this.getSequenceTable();
    SmallMutationsTable smT = this.getSmallMutationsTable();
    GenomeTable gT = this.getGenomeTable();

    Column toFilter = new Column("info", "genome", genomeName);

    for (ChromosomeResult chr : cT.queryTable(toFilter))
      cT.delete(chr.getRowId());

    Iterator<Result> iterator = sT.getResultIterator(toFilter);
    while (iterator.hasNext()) sT.delete(Bytes.toString(iterator.next().getRow()));

    iterator = smT.getResultIterator(toFilter);
    while (iterator.hasNext()) smT.delete(Bytes.toString(iterator.next().getRow()));

    deleteKaryotypes(genomeName);

    gT.delete(genomeName);

//    for (AbstractTable t: new AbstractTable[]{cT, sT, smT, gT})
//      t.close();
    }

  public void deleteKaryotypes(String genomeName) throws IOException
    {
    KaryotypeAberrationTable kT = this.getKaryotypeTable();
    KaryotypeIndexTable kiT = this.getKaryotypeIndexTable();

    Column toFilter = new Column("info", "genome", genomeName);

    Iterator<Result> iterator = kiT.getResultIterator(toFilter);
    while (iterator.hasNext())
      {
      Result result = iterator.next();

      Iterator<Result> abrI = kT.getResultIterator(new Column("info", "karyotype", Bytes.toString(result.getRow())));
      while (abrI.hasNext()) kT.delete(Bytes.toString(abrI.next().getRow()));

      kiT.delete(Bytes.toString(result.getRow()));
      }
    //kiT.close(); kT.close();
    }

  public void disableTables() throws IOException
    {
    super.disableTables(IGCSATables.getTableNames());
    }

  public void deleteTables() throws IOException
    {
    disableTables();
    super.deleteTables(IGCSATables.getTableNames());
    }

  @Override
  public void createTables() throws IOException
    {
    for (IGCSATables table: IGCSATables.values())
      {
      if (!hbaseAdmin.tableExists(table.getTableName()))
        hbaseAdmin.createTable(AbstractTable.getDescriptor(table));
      }
    }

  @Override
  public boolean tablesExist() throws IOException
    {
    for (IGCSATables table: IGCSATables.values())
      {
      boolean exists = this.tableExists(table.getTableName());
      if (!exists) return false;
      }
    return true;
    }

  }
