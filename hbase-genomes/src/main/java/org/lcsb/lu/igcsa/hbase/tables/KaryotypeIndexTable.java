/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.MinimalKaryotype;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.Aneuploidy;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseKaryotype;
import org.lcsb.lu.igcsa.hbase.rows.AberrationRow;
import org.lcsb.lu.igcsa.hbase.rows.KaryotypeIndexRow;

import java.io.IOException;
import java.util.*;

public class KaryotypeIndexTable extends AbstractTable
  {

  public KaryotypeIndexTable(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public String addKaryotype(String karyotypeName, String parentGenome, MinimalKaryotype karyotype) throws IOException
    {
    KaryotypeIndexRow row = new KaryotypeIndexRow(karyotypeName, parentGenome);
    row.addAberrations(karyotype.getAberrations());
    row.addAneuploidies(karyotype.getAneuploidies());

    String rowId = row.getRowIdAsString();
    try
      {
      this.addRow(row);
      }
    catch (IOException ioe)
      {
      return null;
      }
    return rowId;
    }


  @Override
  public KaryotypeIndexResult queryTable(String rowId, Column column) throws IOException
    {
    return createResult((Result) super.queryTable(rowId, column));
    }

  @Override
  public KaryotypeIndexResult queryTable(String rowId) throws IOException
    {
    return createResult((Result) super.queryTable(rowId));
    }

  @Override
  public List<KaryotypeIndexResult> getRows() throws IOException
    {
    return createResults((List<Result>) super.getRows());
    }

  @Override
  public List<KaryotypeIndexResult> queryTable(Column... columns) throws IOException
    {
    return createResults((List<Result>) super.queryTable(columns));
    }

  @Override
  protected List<KaryotypeIndexResult> createResults(List<Result> results)
    {
    List<KaryotypeIndexResult> indexResults = new ArrayList<KaryotypeIndexResult>();
    for (Result r : results)
      indexResults.add(createResult(r));
    return indexResults;
    }

  @Override
  protected KaryotypeIndexResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      KaryotypeIndexResult indexResult = new KaryotypeIndexResult(result.getRow());
      indexResult.addParentGenome(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("genome")));

      for (KeyValue kv : result.list())
        {
        String family = Bytes.toString(kv.getFamily());
        String qualifier = Bytes.toString(kv.getQualifier());
        byte[] value = kv.getValue();

        if (family.equals("abr")) indexResult.addAberration(value);
        else if (family.equals("gain")) indexResult.addAneuploidy(value, true);
        else if (family.equals("loss")) indexResult.addAneuploidy(value, false);
        }

      return indexResult;
      }
    return null;
    }


  public class KaryotypeIndexResult extends AbstractResult
    {
    private String karyotype;
    private String parentGenome;
    //private List<String> abrs;
    private List<Aberration> abrs;
    private List<Aneuploidy> aps;

    protected KaryotypeIndexResult(byte[] rowId)
      {
      super(rowId);
      this.karyotype = Bytes.toString(rowId);
      //abrs = new ArrayList<String>();
      abrs = new ArrayList<Aberration>();
      aps = new ArrayList<Aneuploidy>();
      }

    public String getKaryotype()
      {
      return karyotype;
      }

    public String getParentGenome()
      {
      return parentGenome;
      }

    public void addParentGenome(byte[] parentGenome)
      {
      this.parentGenome = Bytes.toString(parentGenome);
      }

    public List<Aberration> getAberrations()
      {
      return abrs;
      }
    //    public List<String> getAberrations()
    //      {
    //      return abrs;
    //      }

    public void addAberration(byte[] aberration)
      {
      String abr = Bytes.toString(aberration);
      String cyto = abr.substring(0, abr.indexOf("("));

      AberrationTypes type = AberrationTypes.valueOf(cyto);
      abr = abr.replaceFirst(cyto, "");

      List<Band> bands = new ArrayList<Band>();

      for (String bd : abr.split(","))
        {
        bands.add(new Band(bd.substring(0, bd.indexOf(":")), "", new Location(Long.parseLong(bd.substring(bd.indexOf(":") + 1,
                                                                                                          bd.indexOf("-"))),
                                                                              Long.parseLong(bd.substring(bd.indexOf("-") + 1,
                                                                                                          bd.length())))));
        }

      Aberration abrObj = new Aberration(bands, type);

      this.abrs.add(abrObj);
      }

    public List<Aneuploidy> getAneuploidy()
      {
      return aps;
      }

    public void addAneuploidy(byte[] aneuploidy, boolean gain)
      {
      String value = Bytes.toString(aneuploidy);
      Aneuploidy ap = new Aneuploidy(value.substring(0, value.indexOf("(")));

      int count = Integer.valueOf(value.substring(value.indexOf("(") + 1, value.length() - 1));
      if (gain) ap.gain(count);
      else ap.lose(count);

      aps.add(ap);
      }


    }

  }
