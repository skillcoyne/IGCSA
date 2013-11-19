package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.*;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

public abstract class AbstractTable
  {
  static Logger log = Logger.getLogger(AbstractTable.class.getName());


  protected HBaseAdmin admin;
  protected Configuration configuration;

  protected HTable hTable;
  protected String tableName;
  protected byte[] hTableName;
  protected Set<String> families;

  protected Map<String, Set<String>> requiredFields;



  public AbstractTable(Configuration configuration, HBaseAdmin admin, String tableName, Map<String, Set<String>> fields, boolean create) throws IOException
    {
    this.admin = admin;
    this.configuration = configuration;
    this.requiredFields = fields;

    this.setColumnFamilies(fields.keySet().toArray(new String[fields.size()]));


    this.setTableName(tableName);

    if (create)
      this.createTable();

    this.hTable = new HTable(configuration, tableName);
    //this.hTable = getHTable(configuration, tableName);
    }

  private void createTable() throws IOException
    {
    if (this.requiredFields == null || this.requiredFields.keySet().size() <= 0)
      throw new IOException("Column families are not set, table cannot be created.");

    if (!admin.tableExists(Bytes.toBytes(tableName)))
      {
      HTableDescriptor descriptor = new HTableDescriptor(tableName);
      for (String fam : requiredFields.keySet()) // columns
        descriptor.addFamily(new HColumnDescriptor(fam));

      admin.createTable(descriptor);
      }
    }


  public String getTableName()
    {
    return tableName;
    }

  private void setTableName(String tableName)
    {
    this.tableName = tableName;
    this.hTableName = Bytes.toBytes(tableName);
    }

  protected void setColumnFamilies(String[] families)
    {
    this.families = new HashSet<String>();
    for (String str : families)
      this.families.add(str);
    }

  public String[] getColumnFamilies()
    {
    return this.families.toArray(new String[this.families.size()]);
    }


  public Object queryTable(String rowId, Column column) throws IOException
    {
    Get get = new Get(Bytes.toBytes(rowId));

    if (column.hasQualifier() && column.hasValue())
      get.setFilter(new SingleColumnValueFilter(column.getFamliy(), column.getQualifier(), CompareFilter.CompareOp.EQUAL, column.getValue()));
    else if (column.hasQualifier() && !column.hasValue()) // has both family & qualifier, but no value
      get.addColumn(column.getFamliy(), column.getQualifier());
    else if (column.hasFamily()) // only family
      get.addFamily(column.getFamliy());

    return hTable.get(get);
    }

  public Object queryTable(String rowId) throws IOException
    {
    Get get = new Get(Bytes.toBytes(rowId));
    return hTable.get(get);
    }

  public List<? extends Object> getRows() throws IOException
    {
    Scan scan = new Scan();
    ResultScanner scanner = hTable.getScanner(scan);

    return transformScannerResults(scanner);
    }


  public Iterator<Result> getResultIterator(Column... columns) throws IOException
    {
    Scan scan = new Scan();

    for (Column column : columns)
      {
      if (column.hasQualifier() && column.hasValue())
        {
        if (column.hasValue())
          scan.setFilter(new SingleColumnValueFilter(column.getFamliy(), column.getQualifier(), CompareFilter.CompareOp.EQUAL, column.getValue()));
        else if (column.hasQualifier() && !column.hasValue()) // has both family & qualifier, but no value
          scan.addColumn(column.getFamliy(), column.getQualifier());
        else if (column.hasFamily()) // only family
          scan.addFamily(column.getFamliy());
        }
      }
    ResultScanner scanner = hTable.getScanner(scan);
    return scanner.iterator();
    }


  public List<? extends Object> queryTable(Column... columns) throws IOException
    {
    Scan scan = new Scan();

    for (Column column : columns)
      {
      if (column.hasQualifier() && column.hasValue())
        {
        if (column.hasValue())
          scan.setFilter(new SingleColumnValueFilter(column.getFamliy(), column.getQualifier(), CompareFilter.CompareOp.EQUAL, column.getValue()));
        else if (column.hasQualifier() && !column.hasValue()) // has both family & qualifier, but no value
          scan.addColumn(column.getFamliy(), column.getQualifier());
        else if (column.hasFamily()) // only family
          scan.addFamily(column.getFamliy());
        }
      }
    ResultScanner scanner = hTable.getScanner(scan);

    return transformScannerResults(scanner);
    }


  public void updateRow(String rowId, Column... columns) throws IOException
    {
    Result result = hTable.get(new Get(Bytes.toBytes(rowId)));
    if (result == null || !Bytes.toString(result.getRow()).equals(rowId))
      throw new IOException("No row for id " + rowId);

    org.lcsb.lu.igcsa.hbase.rows.Row row = new GenericRow(rowId);
    for (Column c : columns)
      row.addColumn(c);

    addRow(row);
    }

  public void addRow(org.lcsb.lu.igcsa.hbase.rows.Row row) throws IOException
    {
    Put put = new Put(row.getRowId());

    if (!row.isRowIdCorrect())
      throw new IOException("Row id is incorrect: " + row.getRowIdAsString());

    for (Column col : row.getColumns())
      {
      if (!col.hasValue())
        throw new IllegalArgumentException("Column has no value");

      put.add(col.getFamliy(), col.getQualifier(), col.getValue());
      }

    hTable.put(put);
    }


  public void delete(String rowId) throws IOException
    {
    if (queryTable(rowId) != null)
      {
      log.info("Deleting " + rowId);
      Delete del = new Delete(Bytes.toBytes(rowId));
      hTable.delete(del);
      }
    }

  private List<Result> transformScannerResults(ResultScanner scanner)
    {
    List<Result> results = new ArrayList<Result>();
    for (Result r : scanner)
      results.add(r);

    return results;
    }

  protected abstract List<? extends AbstractResult> createResults(List<Result> results);

  protected abstract AbstractResult createResult(Result result);


  }
