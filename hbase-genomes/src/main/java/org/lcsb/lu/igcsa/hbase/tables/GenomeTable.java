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

public class GenomeTable extends AbstractTable
  {
  static Logger log = Logger.getLogger(GenomeTable.class.getName());

  private static final Map<String, Set<String>> reqFields;

  static
    {
    reqFields = new HashMap<String, Set<String>>();
    reqFields.put("info", new HashSet<String>(Arrays.asList("name")));
    reqFields.put("chr", new HashSet<String>(Arrays.asList("list")));
    }

  public GenomeTable(Configuration configuration, HBaseAdmin admin, String tableName, boolean create) throws IOException
    {
    super(configuration, admin, tableName, reqFields, create);
    }

  @Override
  public List<GenomeResult> getRows() throws IOException
    {
    List<Result> rows = (List<Result>) super.getRows();
    return  this.createResults(rows);
    }

  @Override
  public GenomeResult queryTable(String rowId, Column column) throws IOException
    {
    Result result = (Result) super.queryTable(rowId, column);
    return this.createResult(result);
    }

  @Override
  public GenomeResult queryTable(String rowId) throws IOException
    {
    Result result = (Result) super.queryTable(rowId);
    return this.createResult(result);
    }

  @Override
  public List<GenomeResult> queryTable(Column column) throws IOException
    {
    List<Result> results = (List<Result>) super.queryTable(column);
    return  this.createResults(results);
    }

  @Override
  protected List<GenomeResult> createResults(List<Result> results)
    {
    List<GenomeResult> genomeResults = new ArrayList<GenomeResult>();
    for (Result r: results)
      genomeResults.add( createResult(r) );

    return genomeResults;
    }

  @Override
  protected GenomeResult createResult(Result result)
    {
    GenomeResult genomeResult = new GenomeResult(result.getRow());

    for (KeyValue kv: result.list())
      {
      String family = Bytes.toString(kv.getFamily());
      String qualifier = Bytes.toString(kv.getQualifier());
      byte[] value = kv.getValue();

      if (family.equals("info"))
        {
        if (qualifier.equals("name"))
          genomeResult.setName(value);
        if (qualifier.equals("parent"))
          genomeResult.setParent(value);
        }
      else if (family.equals("chr") && qualifier.equals("list"))
        genomeResult.setChromosomes(value);
      }

    return genomeResult;
    }




  }
