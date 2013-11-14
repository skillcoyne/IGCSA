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

public class KaryotypeIndexTable extends AbstractTable
  {
  static Logger log = Logger.getLogger(KaryotypeIndexTable.class.getName());

  private static final Map<String, Set<String>> reqFields;
  static
    {
    reqFields = new HashMap<String, Set<String>>();
    reqFields.put("info", new HashSet<String>(Arrays.asList("genome")));
    reqFields.put("abr", new HashSet<String>());
    }


  public KaryotypeIndexTable(Configuration configuration, HBaseAdmin admin, String tableName, boolean create) throws IOException
    {
    super(configuration, admin, tableName, reqFields, create);
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
  public List<KaryotypeIndexResult> queryTable(Column column) throws IOException
    {
    return createResults((List<Result>) super.queryTable(column));
    }

  @Override
  protected List<KaryotypeIndexResult> createResults(List<Result> results)
    {
    List<KaryotypeIndexResult> indexResults = new ArrayList<KaryotypeIndexResult>();
    for (Result r: results)
      indexResults.add( createResult(r) );
    return indexResults;
    }

  @Override
  protected KaryotypeIndexResult createResult(Result result)
    {
    KaryotypeIndexResult indexResult = new KaryotypeIndexResult(result.getRow());

    for (KeyValue kv : result.list())
      {
      String family = Bytes.toString(kv.getFamily());
      //String qualifier = Bytes.toString(kv.getQualifier());
      byte[] value = kv.getValue();

      if (family.equals("abr"))
        indexResult.addAberration(value);
      }

    return indexResult;
    }


  public class KaryotypeIndexResult extends AbstractResult
    {

    private String genome;
    private List<String> abrs;

    protected KaryotypeIndexResult(byte[] rowId)
      {
      super(rowId);
      this.genome = Bytes.toString(rowId);
      abrs = new ArrayList<String>();
      }

    public List<String> getAberrations()
      {
      return abrs;
      }

    public void addAberration(byte[] aberration)
      {
      String abr = Bytes.toString(aberration);
      this.abrs.add(abr);
      }
    }

  }
