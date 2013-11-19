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

public class ChromosomeTable extends AbstractTable
  {
  static Logger log = Logger.getLogger(ChromosomeTable.class.getName());

  private static final Map<String, Set<String>> reqFields;

  static
    {
    reqFields = new HashMap<String, Set<String>>();
    reqFields.put("info", new HashSet<String>(Arrays.asList("genome")));
    reqFields.put("chr", new HashSet<String>(Arrays.asList("length", "segments", "name")));
    }

  public ChromosomeTable(Configuration configuration, HBaseAdmin admin, String tableName, boolean create) throws IOException
    {
    super(configuration, admin, tableName, reqFields, create);
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

      for (KeyValue kv : result.list())
        {
        String family = Bytes.toString(kv.getFamily());
        String qualifier = Bytes.toString(kv.getQualifier());
        byte[] value = kv.getValue();

        if (family.equals("chr"))
          {
          if (qualifier.equals("length"))
            chrResult.setLength(value);
          if (qualifier.equals("segments"))
            chrResult.setSegmentNumber(value);
          if (qualifier.equals("name"))
            chrResult.setChrName(value);
          }
        else if (family.equals("info") && qualifier.equals("genome"))
          chrResult.setGenomeName(value);
        }

      return chrResult;
      }

    return null;
    }


  }
