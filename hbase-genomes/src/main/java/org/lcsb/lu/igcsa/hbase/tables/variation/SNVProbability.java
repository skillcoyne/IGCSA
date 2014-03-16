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
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SNVProbability extends AbstractTable<SNVProbability>
  {
  private static final Log log = LogFactory.getLog(SNVProbability.class);

  public SNVProbability(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public String addSNV(String from, String to, double prob)
    {
    SNVRow row = new SNVRow(SNVRow.createRowId(from, to));
    row.addProbability(prob);
    try
      {
      this.addRow(row);
      }
    catch (IOException e)
      {
      log.error(e);
      return null;
      }
    return row.getRowIdAsString();
    }

  public Map<Character, Probability> getProbabilities() throws IOException, ProbabilityException
    {
    Map<String, Map<Object, Double>> nucProbs = new HashMap<String, Map<Object, Double>>();
    for (Result r: (List<Result>) getRows())
      {
      SNVResult snv = createResult(r);

      if (!nucProbs.containsKey(snv.getFromNuc()))
        nucProbs.put(snv.getFromNuc(), new HashMap<Object, Double>());

      Map<Object, Double> probs = nucProbs.get(snv.getFromNuc());
      probs.put(snv.getToNuc(), snv.getProb());
      }

    Map<Character, Probability> snvProbabilies = new HashMap<Character, Probability>();
    for (String nuc: nucProbs.keySet())
      snvProbabilies.put( Character.valueOf(nuc.charAt(0)), new Probability(nucProbs.get(nuc)));

    return snvProbabilies;
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
    private double prob;

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

    public double getProb()
      {
      return prob;
      }

    public void setProb(byte[] prob)
      {
      this.prob = Bytes.toDouble(prob);
      }
    }

  public static class SNVRow extends Row
    {
    private String fromNuc, toNuc;
    private double prob;

    public static String createRowId(String from, String to)
      {
      return from + "-" + to;
      }

    public SNVRow(String rowId)
      {
      super(rowId);
      String[] nucs = rowId.split("-");
      this.fromNuc = nucs[0];
      this.toNuc = nucs[1];
      }

    public void addProbability(double prob)
      {
      super.addColumn( new Column("prob", "val", prob) );
      this.prob = prob;
      }

    @Override
    public boolean isRowIdCorrect()
      {
      return createRowId(fromNuc, toNuc).equals(this.getRowIdAsString());
      }
    }


  }
