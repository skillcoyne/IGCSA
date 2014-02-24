package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
//import org.joni.ScanEnvironment;
import org.lcsb.lu.igcsa.hbase.rows.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

public abstract class AbstractTable extends HTable
  {
  protected static final Log log = LogFactory.getLog(AbstractTable.class);

  //protected HBaseAdmin admin;
  //protected Configuration configuration;

  //protected HTable hTable;
//  protected String tableName;
//  protected byte[] hTableName;

  public AbstractTable(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public AbstractTable(Configuration conf, byte[] tableName) throws IOException
    {
    super(conf, tableName);
    }

  public AbstractTable(byte[] tableName, HConnection connection) throws IOException
    {
    super(tableName, connection);
    }

  public AbstractTable(Configuration conf, byte[] tableName, ExecutorService pool) throws IOException
    {
    super(conf, tableName, pool);
    }

  public AbstractTable(byte[] tableName, HConnection connection, ExecutorService pool) throws IOException
    {
    super(tableName, connection, pool);
    }

  public static HTableDescriptor getDescriptor(IGCSATables table)
    {
    HTableDescriptor descriptor = new HTableDescriptor(table.getTableName());
    for (String fam : table.getRequiredFamilies().keySet()) // columns
      descriptor.addFamily(new HColumnDescriptor(fam));
    return descriptor;
    }



  //  private void createTable() throws IOException
  //    {
  //    if (this.requiredFields == null || this.requiredFields.keySet().size() <= 0)
  //      throw new IOException("Column families are not set, table cannot be created.");
  //
  ////    if (!admin.tableExists(Bytes.toBytes(tableName)))
  ////      {
  ////      HTableDescriptor descriptor = new HTableDescriptor(tableName);
  ////      for (String fam : requiredFields.keySet()) // columns
  ////        descriptor.addFamily(new HColumnDescriptor(fam));
  ////
  ////      admin.createTable(descriptor);
  ////      }
  //    }


  //  private void setTableName(String tableName)
  //    {
  //    this.tableName = tableName;
  //    this.hTableName = Bytes.toBytes(tableName);
  //    }

  //  protected void setColumnFamilies(String[] families)
  //    {
  //    this.families = new HashSet<String>();
  //    for (String str : families)
  //      this.families.add(str);
  //    }
  //
  //  public String[] getColumnFamilies()
  //    {
  //    return this.families.toArray(new String[this.families.size()]);
  //    }


  public Object queryTable(String rowId, Column column) throws IOException
    {
    Get get = new Get(Bytes.toBytes(rowId));

    if (column.hasQualifier() && column.hasValue())
      get.setFilter(new SingleColumnValueFilter(column.getFamliy(), column.getQualifier(), CompareFilter.CompareOp.EQUAL,
                                                column.getValue()));
    else if (column.hasQualifier() && !column.hasValue()) // has both family & qualifier, but no value
      get.addColumn(column.getFamliy(), column.getQualifier());
    else if (column.hasFamily()) // only family
      get.addFamily(column.getFamliy());

    return this.get(get);
    }

  public Object queryTable(String rowId) throws IOException
    {
    Get get = new Get(Bytes.toBytes(rowId));
    return this.get(get);
    }

  public List<? extends Object> getRows() throws IOException
    {
    Scan scan = new Scan();
    ResultScanner scanner = this.getScanner(scan);

    return transformScannerResults(scanner);
    }

  public Scan getScanFor(Column... columns)
    {
    return getScanFor(CompareFilter.CompareOp.EQUAL, columns);
    }

  public Scan getScanFor(CompareFilter.CompareOp op, Column... columns)
    {
    Scan scan = new Scan();

    FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    for (Column column : columns)
      {
      if (column.hasQualifier() && column.hasValue())
        {
        if (column.hasValue())
          filterList.addFilter(new SingleColumnValueFilter(column.getFamliy(), column.getQualifier(), op, column.getValue()));
        //scan.setFilter(new SingleColumnValueFilter(column.getFamliy(), column.getQualifier(), CompareFilter.CompareOp.EQUAL,
        // column.getValue()));
        else if (column.hasQualifier() && !column.hasValue()) // has both family & qualifier, but no value
          scan.addColumn(column.getFamliy(), column.getQualifier());
        else if (column.hasFamily()) // only family
          scan.addFamily(column.getFamliy());
        }
      }
    scan.setFilter(filterList);

    return scan;
    }

  public Iterator<Result> getScanner(FilterList filters) throws IOException
    {
    Scan scan = new Scan();
    scan.setCaching(300);
    scan.setBatch(100);
    scan.setFilter(filters);
    ResultScanner scanner = this.getScanner(scan);
    return scanner.iterator();
    }

  public Iterator<Result> runScan(Scan scan) throws IOException
    {
    ResultScanner scanner = this.getScanner(scan);
    return scanner.iterator();
    }


  public Iterator<Result> getResultIterator(Column... columns) throws IOException
    {
    ResultScanner scanner = this.getScanner(getScanFor(columns));
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
          scan.setFilter(new SingleColumnValueFilter(column.getFamliy(), column.getQualifier(), CompareFilter.CompareOp.EQUAL,
                                                     column.getValue()));
        else if (column.hasQualifier() && !column.hasValue()) // has both family & qualifier, but no value
          scan.addColumn(column.getFamliy(), column.getQualifier());
        else if (column.hasFamily()) // only family
          scan.addFamily(column.getFamliy());
        }
      }
    ResultScanner scanner = this.getScanner(scan);

    return transformScannerResults(scanner);
    }


  public void updateRow(String rowId, Column... columns) throws IOException
    {
    Result result = this.get(new Get(Bytes.toBytes(rowId)));
    if (result == null || !Bytes.toString(result.getRow()).equals(rowId)) throw new IOException("No row for id " + rowId);

    org.lcsb.lu.igcsa.hbase.rows.Row row = new GenericRow(rowId);
    for (Column c : columns)
      row.addColumn(c);

    addRow(row);
    }

  public void addRow(org.lcsb.lu.igcsa.hbase.rows.Row row) throws IOException
    {
    this.put(getPut(row));
    }


  public Put getPut(org.lcsb.lu.igcsa.hbase.rows.Row row) throws IOException
    {
    Put put = new Put(row.getRowId());

    if (!row.isRowIdCorrect()) throw new IOException("Row id is incorrect: " + row.getRowIdAsString());

    for (Column col : row.getColumns())
      {
      if (!col.hasValue()) throw new IllegalArgumentException("Column has no value");

      put.add(col.getFamliy(), col.getQualifier(), col.getValue());
      }

    return put;
    }

  public void delete(String rowId) throws IOException
    {
    if (queryTable(rowId) != null)
      {
      log.debug("Deleting " + getTableName() + "row: " + rowId);
      Delete del = new Delete(Bytes.toBytes(rowId));
      this.delete(del);
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
