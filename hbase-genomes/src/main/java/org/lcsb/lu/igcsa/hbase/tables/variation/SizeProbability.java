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
import java.util.*;


public class SizeProbability extends AbstractTable<SizeProbability>
  {
  private static final Log log = LogFactory.getLog(SizeProbability.class);

  public SizeProbability(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public String addSizeProbabiilty(String variation, int maxbp, double prob)
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

  public Probability getProbabilityFor(String variation) throws IOException, ProbabilityException
    {
    List<SizeResult> results = queryTable(new Column("var", "name", variation));

    Map<Object, Double> probs = new TreeMap<Object, Double>();
    for(SizeResult sr: results)
      probs.put( sr.getMaxbp(), sr.getProb() );

    return new Probability(probs, 4);
    }


  public Map<String, Probability> getProbabilities() throws IOException, ProbabilityException
    {
    List<String> variations = getVariationList();
    Map<String, Probability> sizeProbs = new HashMap<String, Probability>();

    for (String v: variations)
      sizeProbs.put(v, getProbabilityFor(v));


//    Map<String, TreeMap<Object, Double>> varProbs = new HashMap<String, TreeMap<Object, Double>>();

//    for (Result r: (List<Result>) getRows())
//      {
//      SizeResult size = createResult(r);
//      if (!varProbs.containsKey(size.getVariation()))
//        varProbs.put(size.getVariation(), new TreeMap<Object, Double>());
//
//      TreeMap<Object, Double> probs = varProbs.get(size.getVariation());
//      probs.put(size.getMaxbp(), size.getProb());
//      }

//    for (String var: varProbs.keySet())
//      sizeProbs.put(var, new Probability(varProbs.get(var)));

    return sizeProbs;
    }

  public List<String> getVariationList() throws IOException
    {
    Set<String> vars = new HashSet<String>();
    List<SizeResult> results = queryTable(new Column("bp", "max", 10));
    for (SizeResult sr: results)
      vars.add(sr.getVariation());

    return new ArrayList<String>(vars);
    }

  @Override
  public List<SizeResult> queryTable(Column... columns) throws IOException
    {
    List<SizeResult> results = new ArrayList<SizeResult>();
    for (Result r: (List<Result>) super.queryTable(columns))
      results.add(createResult(r));

    return results;
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
    private double prob;

    public SizeResult(byte[] rowId)
      {
      super(rowId);
      String[] row = this.getRowId().split(":");
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

    public double getProb()
      {
      return prob;
      }

    public void setProbability(byte[] prob)
      {
      this.prob = Bytes.toDouble(prob);
      }
    }

  static class SizeRow extends Row
    {
    private String variation;
    private int maxbp;

    public static String createRowId(String variation, int maxbp)
      {
      return variation + ":" + maxbp;
      }

    public SizeRow(String rowId)
      {
      super(rowId);
      String[] row = rowId.split(":");

      this.variation = row[0];
      this.maxbp = Integer.parseInt(row[1]);

      super.addColumn(new Column("var", "name", variation));
      super.addColumn(new Column("bp", "max", maxbp));
      }

    public void addProbability(double prob)
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
