/**
 * org.lcsb.lu.igcsa.hbase.tables.variation
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.variation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.rows.Row;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class SizeProbability extends AbstractTable<SizeProbability>
  {
  private static final Log log = LogFactory.getLog(SizeProbability.class);

  public SizeProbability(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public String addSizeProbabiilty(String variation, int maxbp, long prob)
    {
    SizeRow row = new SizeRow(SizeRow.createRowId(variation, maxbp));
    row.addProbability(prob);
    try
      {
      this.addRow(row);
      }
    catch (IOException e)
      {
      return null;
      }
    return row.getRowIdAsString();
    }

  @Override
  protected List<SizeResult> createResults(List<Result> results)
    {
    List<SizeResult> sizeResults = new ArrayList<SizeResult>();
    for (Result r: results)
      sizeResults.add(createResult(r));
    return sizeResults;
    }

  @Override
  protected SizeResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      SizeResult size = new SizeResult(result.getRow());
      size.setProbability(result.getValue(Bytes.toBytes("bp"), Bytes.toBytes("prob")));
      return size;
      }
    return null;
    }

  static class SizeResult extends AbstractResult
    {
    private String variation;
    private int maxbp;
    private long prob;

    SizeResult(byte[] rowId)
      {
      super(rowId);
      String[] row = this.getRowId().split("_");
      this.variation = row[0];
      this.maxbp = Integer.parseInt(row[1]);
      }

    public String getVariation()
      {
      return variation;
      }

    public int getMaxbp()
      {
      return maxbp;
      }

    public long getProb()
      {
      return prob;
      }

    public void setProbability(byte[] prob)
      {
      this.prob = Bytes.toLong(prob);
      }
    }

  static class SizeRow extends Row
    {
    private String variation;
    private int maxbp;

    public static String createRowId(String variation, int maxbp)
      {
      return variation + "_" + maxbp;
      }

    public SizeRow(String rowId)
      {
      super(rowId);
      String[] row = rowId.split("_");

      this.variation = row[0];
      this.maxbp = Integer.parseInt(row[1]);

      super.addColumn(new Column("var", "name", variation));
      super.addColumn(new Column("bp", "max", maxbp));
      }

    public void addProbability(long prob)
      {
      super.addColumn(new Column("bp", "prob", prob));
      }

    @Override
    public boolean isRowIdCorrect()
      {
      return createRowId(variation, maxbp).equals(this.getRowIdAsString());
      }
    }
  }
