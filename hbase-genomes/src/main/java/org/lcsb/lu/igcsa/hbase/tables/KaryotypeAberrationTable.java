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
import org.lcsb.lu.igcsa.generator.Aberration;

import java.io.IOException;
import java.util.*;

public class KaryotypeAberrationTable extends AbstractTable
  {
  static Logger log = Logger.getLogger(KaryotypeAberrationTable.class.getName());

  private static final Map<String, Set<String>> reqFields;

  static
    {
    reqFields = new HashMap<String, Set<String>>();
    reqFields.put("info", new HashSet<String>(Arrays.asList("karyotype")));
    reqFields.put("abr", new HashSet<String>(Arrays.asList("type", "chr1", "loc1")));
    }

  public KaryotypeAberrationTable(Configuration configuration, HBaseAdmin admin, String tableName, boolean create) throws IOException
    {
    super(configuration, admin, tableName, reqFields, create);
    }

  @Override
  public AberrationResult queryTable(String rowId, Column column) throws IOException
    {
    return createResult((Result) super.queryTable(rowId, column));
    }

  @Override
  public AberrationResult queryTable(String rowId) throws IOException
    {
    return createResult((Result) super.queryTable(rowId));
    }

  @Override
  public List<AberrationResult> getRows() throws IOException
    {
    return createResults((List<Result>) super.getRows());
    }

  @Override
  public List<AberrationResult> queryTable(Column... columns) throws IOException
    {
    return createResults((List<Result>) super.queryTable(columns));
    }

  @Override
  protected List<AberrationResult> createResults(List<Result> results)
    {
    List<AberrationResult> aberrationResults = new ArrayList<AberrationResult>();
    for (Result r : results)
      aberrationResults.add(createResult(r));

    return aberrationResults;
    }

  @Override
  protected AberrationResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      AberrationResult aberrationResult = new AberrationResult(result.getRow());

      KeyValue kv = result.getColumn(Bytes.toBytes("info"), Bytes.toBytes("karyotype")).get(0);
      aberrationResult.setGenome(kv.getValue());

      kv = result.getColumn(Bytes.toBytes("abr"), Bytes.toBytes("type")).get(0);
      aberrationResult.setAbrType(kv.getValue());

      // chances of there being very many aberrations (like more than 3) are pretty much nil
      int i = 1;
      while(true)
        {
        if (result.getColumn(Bytes.toBytes("abr"), Bytes.toBytes("chr" + i)).size() <= 0 )
          break;

        KeyValue kvc = result.getColumn(Bytes.toBytes("abr"), Bytes.toBytes("chr" + i)).get(0);
        KeyValue kvl = result.getColumn(Bytes.toBytes("abr"), Bytes.toBytes("loc" + i)).get(0);

        aberrationResult.addAberrationDefinitions(kvc.getValue(), kvl.getValue());
        i++;
        }

      return aberrationResult;
      }

    return null;
    }
  }
