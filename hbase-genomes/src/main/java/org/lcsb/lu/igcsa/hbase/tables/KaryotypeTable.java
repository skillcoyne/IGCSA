/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class KaryotypeTable extends AbstractTable
  {
  static Logger log = Logger.getLogger(KaryotypeTable.class.getName());

  private static final Map<String, Set<String>> reqFields;

  static
    {
    reqFields = new HashMap<String, Set<String>>();
    reqFields.put("info", new HashSet<String>(Arrays.asList("karyotype")));
    reqFields.put("abr", new HashSet<String>(Arrays.asList("type", "chr1", "loc1")));
    }

  public KaryotypeTable(Configuration configuration, HBaseAdmin admin, String tableName, boolean create) throws IOException
    {
    super(configuration, admin, tableName, reqFields, create);
    }

  @Override
  public KaryotypeResult queryTable(String rowId, Column column) throws IOException
    {
    return createResult((Result) super.queryTable(rowId, column));
    }

  @Override
  public KaryotypeResult queryTable(String rowId) throws IOException
    {
    return createResult((Result) super.queryTable(rowId));
    }

  @Override
  public List<KaryotypeResult> getRows() throws IOException
    {
    return createResults((List<Result>) super.getRows());
    }

  @Override
  public List<KaryotypeResult> queryTable(Column... columns) throws IOException
    {
    return createResults((List<Result>) super.queryTable(columns));
    }

  @Override
  protected List<KaryotypeResult> createResults(List<Result> results)
    {
    List<KaryotypeResult> karyotypeResults = new ArrayList<KaryotypeResult>();
    for (Result r : results)
      karyotypeResults.add(createResult(r));

    return karyotypeResults;
    }

  @Override
  protected KaryotypeResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      KaryotypeResult karyotypeResult = new KaryotypeResult(result.getRow());

      KeyValue kv = result.getColumn(Bytes.toBytes("info"), Bytes.toBytes("genome")).get(0);
      karyotypeResult.setGenome(kv.getValue());

      kv = result.getColumn(Bytes.toBytes("abr"), Bytes.toBytes("type")).get(0);
      karyotypeResult.setAbrType(kv.getValue());

      // chances of there being very many aberrations (like more than 3) are pretty much nil
      for (int i = 1; i <= 10; i++)
        {
        KeyValue kvc = result.getColumn(Bytes.toBytes("abr"), Bytes.toBytes("chr" + i)).get(0);
        KeyValue kvl = result.getColumn(Bytes.toBytes("abr"), Bytes.toBytes("chr" + i)).get(0);

        karyotypeResult.addAberrationDefinitions(kvc.getValue(), kvl.getValue());
        }

      return karyotypeResult;
      }

    return null;
    }
  }
