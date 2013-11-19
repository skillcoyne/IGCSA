/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.*;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HBaseGenomeAdmin
  {
  static Logger log = Logger.getLogger(HBaseGenomeAdmin.class.getName());

  private Configuration conf;
  private HBaseAdmin hbaseAdmin;

  private GenomeTable gT;
  private ChromosomeTable cT;
  private SequenceTable sT;
  private SmallMutationsTable smT;
  private KaryotypeIndexTable kiT;
  private KaryotypeTable kT;

  private GenomeResult lastRetrievedGenome;


  private static HBaseGenomeAdmin adminInstance;

  public static HBaseGenomeAdmin getHBaseGenomeAdmin() throws IOException
    {
    if (adminInstance == null)
      adminInstance = new HBaseGenomeAdmin(HBaseConfiguration.create());

    return adminInstance;
    }

  public static HBaseGenomeAdmin getHBaseGenomeAdmin(Configuration configuration) throws IOException
    {
    if (adminInstance == null)
      adminInstance = new HBaseGenomeAdmin(configuration);

    return adminInstance;
    }

  private HBaseGenomeAdmin(Configuration configuration) throws IOException, MasterNotRunningException
    {
    this.conf = configuration;
    this.conf.setInt("timeout", 10);
    this.hbaseAdmin = new HBaseAdmin(conf);
    this.createTables();
    }

  protected GenomeTable getGenomeTable()
    {
    return gT;
    }

  protected ChromosomeTable getChromosomeTable()
    {
    return cT;
    }

  protected SequenceTable getSequenceTable()
    {
    return sT;
    }

  protected SmallMutationsTable getSmallMutationsTable()
    {
    return smT;
    }

  protected KaryotypeIndexTable getKaryotypeIndexTable()
    {
    return kiT;
    }

  protected KaryotypeTable getKaryotypeTable()
    {
    return kT;
    }

  public List<HBaseGenome> retrieveGenomes() throws IOException
    {
    List<HBaseGenome> genomes = new ArrayList<HBaseGenome>();
    for(GenomeResult r: gT.getRows())
      genomes.add( new HBaseGenome(r) );
    return genomes;
    }

  public HBaseGenome getGenome(String genomeName) throws IOException
    {
    GenomeResult result = gT.queryTable(genomeName);
    return (result != null)? new HBaseGenome(result): null;
    }

  public HBaseGenome getReference() throws IOException
    {
    return new HBaseGenome((GenomeResult) gT.queryTable(new Column("info", "parent", "reference")));
    }


  /**
   * All
   *
   * @return
   * @throws IOException
   */
  public List<KaryotypeResult> retrieveKaryotypes() throws IOException
    {
    return this.kT.getRows();
    }

  public void deleteGenome(String genomeName) throws IOException
    {
    Column toFilter = new Column("info", "genome", genomeName);

    for (ChromosomeResult chr: cT.queryTable(toFilter))
      cT.delete(chr.getRowId());

    Iterator<Result> iterator = sT.getResultIterator(toFilter);
    while (iterator.hasNext())
      sT.delete(Bytes.toString(iterator.next().getRow()));

    iterator =  smT.getResultIterator(toFilter);
    while (iterator.hasNext())
      smT.delete(Bytes.toString(iterator.next().getRow()));

    iterator = kT.getResultIterator(toFilter);
    while (iterator.hasNext())
      kT.delete(Bytes.toString(iterator.next().getRow()));

    kiT.delete(genomeName);
    gT.delete(genomeName);
    }

  public void closeConections() throws IOException
    {
    hbaseAdmin.close();
    }

  public void disableTables() throws IOException
    {
    for (String t : new String[]{"genome", "chromosome", "sequence", "small_mutations", "karytoype_index", "karyotype"})
      {
      if (hbaseAdmin.isTableEnabled(t))
        hbaseAdmin.disableTable(t);
      }
    }

  public void deleteTables() throws IOException
    {
    disableTables();
    for (String t : new String[]{"genome", "chromosome", "sequence", "small_mutations", "karyotype_index", "karyotype"})
      {
      if (hbaseAdmin.tableExists(t))
        hbaseAdmin.deleteTable(t);
      }
    }

  public void createTables()
    {
    boolean create = false;
    try
      {
      if (hbaseAdmin.getTableNames().length < 6)
        {
        log.info("Creating tables");
        create = true;
        }
      // I should really just instantiate these as needed
      gT = new GenomeTable(this.conf, this.hbaseAdmin, "genome", create);
      cT = new ChromosomeTable(this.conf, this.hbaseAdmin, "chromosome", create);
      sT = new SequenceTable(this.conf, this.hbaseAdmin, "sequence", create);
      smT = new SmallMutationsTable(this.conf, this.hbaseAdmin, "small_mutations", create);
      kiT = new KaryotypeIndexTable(this.conf, this.hbaseAdmin, "karyotype_index", create);
      kT = new KaryotypeTable(this.conf, this.hbaseAdmin, "karyotype", create);
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }

    }


  }
