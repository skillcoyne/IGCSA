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
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class ChromosomeTable extends AbstractTable
  {
  private static final Map<String, Set<String>> reqFields;

  static
    {
    reqFields = new HashMap<String, Set<String>>();
    reqFields.put("info", new HashSet<String>(Arrays.asList("genome")));
    reqFields.put("chr", new HashSet<String>(Arrays.asList("length", "segments", "name")));
    }

  public ChromosomeTable(Configuration configuration, HBaseAdmin admin, String tableName) throws IOException
    {
    super(configuration, admin, tableName, reqFields);
    }

  @Override
  public ChromosomeResult queryTable(String rowId, Column column) throws IOException
    {
    Result result = (Result) super.queryTable(rowId, column);
    return this.createResult(result);
    }

  @Override
  public ChromosomeResult queryTable(String rowId) throws IOException
    {
    Result result = (Result) super.queryTable(rowId);
    return this.createResult(result);
    }

  @Override
  public List<ChromosomeResult> getRows() throws IOException
    {
    List<Result> results = (List<Result>) super.getRows();
    return (List<ChromosomeResult>) createResults(results);
    }

  @Override
  public List<ChromosomeResult> queryTable(Column... columns) throws IOException
    {
    List<Result> results = (List<Result>) super.queryTable(columns);
    return (List<ChromosomeResult>) createResults(results);
    }

  @Override
  protected List<? extends AbstractResult> createResults(List<Result> results)
    {
    List<ChromosomeResult> chrResults = new ArrayList<ChromosomeResult>();
    for (Result r : results)
      chrResults.add(this.createResult(r));
    return chrResults;
    }

  @Override
  protected ChromosomeResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      ChromosomeResult chrResult = new ChromosomeResult(result.getRow());

      byte[] chrFam = Bytes.toBytes("chr");

      chrResult.setLength( result.getValue( chrFam, Bytes.toBytes("length")) );
      chrResult.setSegmentNumber( result.getValue( chrFam, Bytes.toBytes("segments")) );
      chrResult.setChrName( result.getValue(chrFam, Bytes.toBytes("name")) );
      chrResult.setGenomeName( result.getValue(Bytes.toBytes("info"), Bytes.toBytes("genome")) );

      return chrResult;
      }

    return null;
    }

  public ChromosomeResult incrementSize(String rowId, long segment, long length) throws IOException
    {
    Increment inc = new Increment( Bytes.toBytes(rowId) );

    if (segment > 0)
      inc.addColumn(Bytes.toBytes("chr"), Bytes.toBytes("segments"), segment);

    if (length > 0)
      inc.addColumn(Bytes.toBytes("chr"), Bytes.toBytes("length"), length);

    Result r = this.hTable.increment(inc);

    return this.createResult(r);
    }



  }
