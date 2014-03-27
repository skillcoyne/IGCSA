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
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;
import java.util.*;


public class VariationCountPerBin extends AbstractTable<VariationCountPerBin>
  {
  private static final Log log = LogFactory.getLog(VariationCountPerBin.class);

  public VariationCountPerBin(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }


  public String addFragment(String chromosome, String variation, String varClass, int count, int gcMin, int gcMax, int fragments)
    {
    if (VCPBRow.getRowCounter() > fragments) VCPBRow.resetRowCounter();

    VCPBRow row = new VCPBRow(VCPBRow.createRowId(chromosome, variation, gcMin, gcMax));
    row.addChromosome(chromosome);
    row.addVariation(variation, varClass, count);
    row.addGCRange(gcMin, gcMax, VCPBRow.getRowCounter());
    try
      {
      this.addRow(row);
      VCPBRow.incrementRowCounter();
      }
    catch (IOException e)
      {
      return null;
      }
    return row.getRowIdAsString();
    }


  public List<VCPBResult> getFragment(String chromosome, int gcMin, int gcMax, int fragmentNumber,
                                      List<String> variations) throws IOException
    {
    List<VCPBResult> results = new ArrayList<VCPBResult>();

    for (String var: variations)
      {
      String rowId = VCPBRow.createRowId(chromosome, var, gcMin, gcMax, fragmentNumber);
      VCPBResult result = this.queryTable(rowId);
      if (result != null)
        results.add(result);
      }

    return results;
    }

  @Override
  public VCPBResult queryTable(String rowId, Column column) throws IOException
    {
    Result result = (Result) super.queryTable(rowId, column);
    return this.createResult(result);
    }

  @Override
  public VCPBResult queryTable(String rowId) throws IOException
    {
    Result result = (Result) super.queryTable(rowId);
    return this.createResult(result);
    }

  @Override
  public List<? extends Object> queryTable(Column... columns) throws IOException
    {
    return super.queryTable(columns);
    }

  @Override
  public List<VCPBResult> createResults(List<Result> results)
    {
    List<VCPBResult> varResults = new ArrayList<VCPBResult>();
    for (Result r : results)
      varResults.add(createResult(r));

    return varResults;
    }

  @Override
  public VCPBResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      VCPBResult varResult = new VCPBResult(result.getRow());

      byte[] chrFam = Bytes.toBytes("chr");
      varResult.setChromosome(result.getValue(chrFam, Bytes.toBytes("name")));

      byte[] varFam = Bytes.toBytes("var");
      varResult.setVariationName(result.getValue(varFam, Bytes.toBytes("name")));
      varResult.setVariationClass(result.getValue(varFam, Bytes.toBytes("class")));
      varResult.setVarCount(result.getValue(varFam, Bytes.toBytes("count")));

      byte[] gcFam = Bytes.toBytes("gc");
      varResult.setGCMin(result.getValue(gcFam, Bytes.toBytes("min")));
      varResult.setGCMax(result.getValue(gcFam, Bytes.toBytes("max")));
      varResult.setFragmentNum(result.getValue(gcFam, Bytes.toBytes("frag")));

      return varResult;
      }
    return null;
    }
  }
