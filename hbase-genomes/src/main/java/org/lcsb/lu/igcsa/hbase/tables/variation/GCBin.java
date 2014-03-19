package org.lcsb.lu.igcsa.hbase.tables.variation;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.Row;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * org.lcsb.lu.igcsa.hbase.tables.variation
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GCBin extends AbstractTable<GCBin>
  {
  static Logger log = Logger.getLogger(GCBin.class.getName());

  public GCBin(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }


  public GCResult getBinFor(String chr, int gcContent) throws IOException
    {
    log.info("Get GC bin: " + chr + " " + gcContent);
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("chr"), Bytes.toBytes("name"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chr)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("gc"), Bytes.toBytes("min"), CompareFilter.CompareOp.LESS_OR_EQUAL,
                                                  Bytes.toBytes(gcContent)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("gc"), Bytes.toBytes("max"), CompareFilter.CompareOp.GREATER_OR_EQUAL,
                                                  Bytes.toBytes(gcContent)));

    Scan scan = new Scan();
    scan.setFilter(filters);

    ResultScanner scanner = this.getScanner(scan);
    Iterator<Result> rI =  scanner.iterator();

    if (!rI.hasNext())
      {
      throw new IOException("Failed to retrieve any GC bins for chr " + chr + " GC=" + gcContent);
      }

    // only expect one result
    GCResult gcResult = createResult(rI.next());
    if (rI.hasNext())
      log.warn("Found multiple matches for " + chr + " " + gcContent + " returning only the first.");

    return gcResult;
    }


  public String addBin(String chr, int min, int max, int total)
    {
    GCRow row = new GCRow(GCRow.createRowId(chr, min, max));
    row.addFragmentTotal(total);

    try
      {
      this.addRow(row);
      }
    catch (IOException e)
      {
      log.warn(e);
      return null;
      }

    return row.getRowIdAsString();
    }

  @Override
  public List<GCResult> createResults(List<Result> results)
    {
    List<GCResult> gcResults = new ArrayList<GCResult>();
    for (Result r: results)
      gcResults.add(createResult(r));
    return gcResults;
    }

  @Override
  public GCResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      GCResult gcResult = new GCResult(result.getRow());
      gcResult.setChromosome(result.getValue(Bytes.toBytes("chr"), Bytes.toBytes("name")));

      byte[] family = Bytes.toBytes("gc");
      gcResult.setMin(result.getValue(family, Bytes.toBytes("min")));
      gcResult.setMax(result.getValue(family, Bytes.toBytes("max")));

      gcResult.setTotalFragments(result.getValue(Bytes.toBytes("frag"), Bytes.toBytes("total")));

      return gcResult;
      }
    return null;
    }

  public static class GCResult extends AbstractResult
    {
    private String chromosome;
    private int min, max, totalFragments;

    public GCResult(byte[] rowId)
      {
      super(rowId);
      }

    public String getChromosome()
      {
      return chromosome;
      }

    public void setChromosome(byte[] chromosome)
      {
      this.chromosome = Bytes.toString(chromosome);
      }

    public int getMin()
      {
      return min;
      }

    public void setMin(byte[] min)
      {
      this.min = Bytes.toInt(min);
      }

    public int getMax()
      {
      return max;
      }

    public void setMax(byte[] max)
      {
      this.max = Bytes.toInt(max);
      }

    public int getTotalFragments()
      {
      return totalFragments;
      }

    public void setTotalFragments(byte[] totalFragments)
      {
      this.totalFragments = Bytes.toInt(totalFragments);
      }
    }

  public static class GCRow extends Row
    {
    private String chromosome;
    private int gcMin, gcMax, totalFragments;

    public static String createRowId(String chr, int min, int max)
      {
      return chr + ":" + min + "-" + max;
      }

    public GCRow(String rowId)
      {
      super(rowId);
      this.chromosome = rowId.substring(0, rowId.indexOf(":"));
      String[] gc = rowId.substring(rowId.indexOf(":")+1, rowId.length()).split("-");
      this.gcMin = Integer.parseInt(gc[0]);
      this.gcMax = Integer.parseInt(gc[1]);

      this.addColumn(new Column("chr", "name", chromosome));
      this.addColumn(new Column("gc", "min", gcMin));
      this.addColumn(new Column("gc", "max", gcMax));
      }

    public void addFragmentTotal(int total)
      {
      this.addColumn(new Column("frag", "total", total));
      }

    @Override
    public boolean isRowIdCorrect()
      {
      return createRowId(chromosome, gcMin, gcMax).equals(this.getRowIdAsString());
      }
    }

  }
