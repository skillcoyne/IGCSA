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
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HBaseGenomeAdmin
  {
  private static final Log log = LogFactory.getLog(HBaseGenomeAdmin.class);

  private Configuration conf;
  private HBaseAdmin hbaseAdmin;
  private static HBaseGenomeAdmin adminInstance;

  public static HBaseGenomeAdmin getHBaseGenomeAdmin() throws IOException
    {
    //if (adminInstance == null)
    adminInstance = new HBaseGenomeAdmin(HBaseConfiguration.create());

    return adminInstance;
    }

  public static HBaseGenomeAdmin getHBaseGenomeAdmin(Configuration configuration) throws IOException
    {
    //if (adminInstance == null)
    adminInstance = new HBaseGenomeAdmin(configuration);

    return adminInstance;
    }

  private HBaseGenomeAdmin(Configuration configuration) throws IOException
    {
    this.conf = configuration;
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

  public List<HBaseGenome> retrieveGenomes() throws IOException
    {
    GenomeTable gT = this.getGenomeTable();
    List<HBaseGenome> genomes = new ArrayList<HBaseGenome>();
    for (GenomeResult r : gT.getRows())
      genomes.add(new HBaseGenome(r));
    gT.close();
    return genomes;
    }

  public HBaseGenome getGenome(String genomeName) throws IOException
    {
    GenomeTable gT = this.getGenomeTable();
    GenomeResult result = gT.queryTable(genomeName);
    gT.close();
    return (result != null) ? new HBaseGenome(result) : null;
    }

  public HBaseKaryotype getKaryotype(String karyotypeName) throws IOException
    {
    KaryotypeIndexTable kiT = this.getKaryotypeIndexTable();
    KaryotypeIndexTable.KaryotypeIndexResult result = kiT.queryTable(karyotypeName);
    kiT.close();
    return (result != null) ? new HBaseKaryotype(result) : null;
    }


  public HBaseGenome getReference() throws IOException
    {
    GenomeTable gT = this.getGenomeTable();
    HBaseGenome genome = new HBaseGenome((GenomeResult) gT.queryTable(new Column("info", "parent", "reference")));
    gT.close();
    return genome;
    }


  /**
   * All
   *
   * @return
   * @throws IOException
   */
  public List<AberrationResult> retrieveKaryotypes() throws IOException
    {
    KaryotypeAberrationTable kT = this.getKaryotypeTable();
    List<AberrationResult> list =  kT.getRows();
    kT.close();
    return list;
    }

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

    for (AbstractTable t: new AbstractTable[]{cT, sT, smT, gT})
      t.close();
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
    kiT.close(); kT.close();
    }

  public void closeConections() throws IOException
    {
    hbaseAdmin.close();
    }

  public void disableTables() throws IOException
    {
    for (IGCSATables tb : IGCSATables.values())
      {
      String t = tb.getTableName();
      if (hbaseAdmin.tableExists(t) && hbaseAdmin.isTableEnabled(t)) hbaseAdmin.disableTable(t);
      }
    }

  public void deleteTables() throws IOException
    {
    disableTables();
    for (IGCSATables tb : IGCSATables.values())
      {
      String t = tb.getTableName();
      if (hbaseAdmin.tableExists(t)) hbaseAdmin.deleteTable(t);
      }
    }

  public void createTables() throws IOException
    {
    for (IGCSATables table: IGCSATables.values())
      {
      if (!hbaseAdmin.tableExists(table.getTableName()))
        hbaseAdmin.createTable(AbstractTable.getDescriptor(table));
      }
    }


  }
