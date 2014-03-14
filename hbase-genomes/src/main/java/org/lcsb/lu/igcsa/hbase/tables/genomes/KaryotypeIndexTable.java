/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.MinimalKaryotype;
import org.lcsb.lu.igcsa.hbase.rows.KaryotypeIndexRow;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.Column;

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

  public KaryotypeIndexResult getKaryotype(String rowId) throws IOException
    {
    return this.queryTable(rowId);
    }

  public List<KaryotypeIndexResult> getKaryotypes(String parentGenome) throws IOException
    {
    return this.queryTable(new Column("info", "genome", parentGenome));
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

        if (family.equals("abr"))
          indexResult.addAberration(value);
        else if (family.equals("gain"))
          indexResult.addAneuploidy(value, true);
        else if (family.equals("loss"))
          indexResult.addAneuploidy(value, false);
        }

      return indexResult;
      }
    return null;
    }


  }
