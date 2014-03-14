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


public class SNVProbability extends AbstractTable
  {
  private static final Log log = LogFactory.getLog(SNVProbability.class);

  public SNVProbability(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public String addSNV(String from, String to, long prob)
    {
    SNVRow row = new SNVRow(SNVRow.createRowId(from, to));
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
  protected List<SNVResult> createResults(List<Result> results)
    {
    List<SNVResult> snvs = new ArrayList<SNVResult>();
    for (Result r : results)
      snvs.add(createResult(r));
    return snvs;
    }

  @Override
  protected SNVResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      SNVResult snv = new SNVResult(result.getRow());
      snv.setProb(result.getValue(Bytes.toBytes("prob"), Bytes.toBytes("val")));
      return snv;
      }
    return null;
    }

  public static class SNVResult extends AbstractResult
    {
    private String fromNuc, toNuc;
    private long prob;

    public SNVResult(byte[] rowId)
      {
      super(rowId);

      String[] snv = this.getRowId().split("-");
      this.fromNuc = snv[0];
      this.toNuc = snv[1];
      }

    public String getFromNuc()
      {
      return fromNuc;
      }

    public String getToNuc()
      {
      return toNuc;
      }

    public long getProb()
      {
      return prob;
      }

    public void setProb(byte[] prob)
      {
      this.prob = Bytes.toLong(prob);
      }
    }

  public static class SNVRow extends Row
    {
    private String fromNuc, toNuc;
    private long prob;

    public static String createRowId(String from, String to)
      {
      return from + "-" + to;
      }

    public SNVRow(String rowId)
      {
      super(rowId);
      }

    public void addProbability(long prob)
      {
      super.addColumn( new Column("prob", "val", prob) );
      this.prob = prob;
      }

    @Override
    public boolean isRowIdCorrect()
      {
      return createRowId(fromNuc, toNuc).equals(this.getRowId());
      }
    }


  }
