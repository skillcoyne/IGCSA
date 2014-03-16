/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.rows.GenomeRow;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;
import java.util.*;

public class GenomeTable extends AbstractTable<GenomeTable>
  {

  public GenomeTable(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public List<GenomeResult> getGenomes() throws IOException
    {
    return this.getRows();
    }

  public List<GenomeResult> getReferenceGenomes() throws IOException
    {
    return this.queryTable(new Column("info", "parent", "reference"));
    }

  public GenomeResult getGenome(String genomeName) throws IOException
    {
    return this.createResult(this.get( new Get(Bytes.toBytes(genomeName)) ));
    }

  public String addGenome(String genomeName, String parentName) throws IOException
    {
    if (parentName != null && this.queryTable(parentName) == null) throw new IOException("No genome matching parent: " + parentName);

    String rowId;
    GenomeResult result = this.queryTable(genomeName);
    if (result == null)
      {
      GenomeRow row = new GenomeRow(genomeName);
      row.addParentColumn(parentName);

      try
        {
        this.addRow(row);
        }
      catch (IOException ioe)
        {
        return null;
        }
      rowId = row.getRowIdAsString();
      }
    else
      rowId = result.getRowId();

    return rowId;
    }

  @Override
  public List<GenomeResult> getRows() throws IOException
    {
    List<Result> rows = (List<Result>) super.getRows();
    return this.createResults(rows);
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
  public List<GenomeResult> queryTable(Column... columns) throws IOException
    {
    List<Result> results = (List<Result>) super.queryTable(columns);
    return this.createResults(results);
    }

  @Override
  protected List<GenomeResult> createResults(List<Result> results)
    {
    List<GenomeResult> genomeResults = new ArrayList<GenomeResult>();
    for (Result r : results)
      genomeResults.add(createResult(r));

    return genomeResults;
    }

  @Override
  protected GenomeResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      GenomeResult genomeResult = new GenomeResult(result.getRow());

      for (KeyValue kv : result.list())
        {
        String family = Bytes.toString(kv.getFamily());
        String qualifier = Bytes.toString(kv.getQualifier());
        byte[] value = kv.getValue();

        if (family.equals("info"))
          {
          if (qualifier.equals("name")) genomeResult.setName(value);
          if (qualifier.equals("parent")) genomeResult.setParent(value);
          }
        else if (family.equals("chr") && qualifier.equals("list")) genomeResult.setChromosomes(value);
        }

      return genomeResult;
      }

    return null;
    }


  }
