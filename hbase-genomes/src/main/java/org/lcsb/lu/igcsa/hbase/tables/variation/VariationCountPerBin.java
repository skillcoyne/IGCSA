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
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;
import java.util.*;


public class VariationCountPerBin extends AbstractTable
  {
  private static final Log log = LogFactory.getLog(VariationCountPerBin.class);

  public VariationCountPerBin(Configuration conf, String tableName) throws IOException
    {
    super(conf, tableName);
    }

  public String addFragment(String chromosome, String variation, String varClass, long count, long gcMin, long gcMax)
    {
    VCPBRow row = new VCPBRow( VCPBRow.createRowId(chromosome, count, variation, gcMin, gcMax) );
    row.addChromosome(chromosome);
    row.addVariation(variation, varClass, count);
    row.addGCRange(gcMin, gcMax);
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

  public List<VCBPResult> getFragmentsFor(String chromosome, long gcMin, long gcMax) throws IOException
    {
    // Actually I don't want them all, I want to grab a random one out of the pile
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("chr"), Bytes.toBytes("name"), CompareFilter.CompareOp.EQUAL,
        Bytes.toBytes(chromosome)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("gc"), Bytes.toBytes("min"), CompareFilter.CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(gcMin)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("gc"), Bytes.toBytes("max"), CompareFilter.CompareOp.LESS_OR_EQUAL, Bytes.toBytes(gcMax)));

    Scan scan = new Scan();
    scan.setFilter(filters);

    List<VCBPResult> variations = new ArrayList<VCBPResult>();
    ResultScanner scanner = this.getScanner(scan);
    Iterator<Result> rI = scanner.iterator();
    while (rI.hasNext())
      variations.add( createResult(rI.next()) );

    return variations;
    }

  @Override
  public VCBPResult queryTable(String rowId, Column column) throws IOException
    {
    Result result = (Result) super.queryTable(rowId, column);
    return this.createResult(result);
    }

  @Override
  public VCBPResult queryTable(String rowId) throws IOException
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
  protected List<VCBPResult> createResults(List<Result> results)
    {
    List<VCBPResult> varResults = new ArrayList<VCBPResult>();
    for (Result r : results)
      varResults.add(createResult(r));

    return varResults;
    }

  @Override
  protected VCBPResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      VCBPResult varResult = new VCBPResult(result.getRow());

      byte[] chrFam = Bytes.toBytes("chr");
      varResult.setChromosome(result.getValue(chrFam, Bytes.toBytes("name")));

      byte[] varFam = Bytes.toBytes("var");
      varResult.setVariationName(result.getValue(varFam, Bytes.toBytes("name")));
      varResult.setVariationClass(result.getValue(varFam, Bytes.toBytes("class")));
      varResult.setVarCount(result.getValue(varFam, Bytes.toBytes("count")));

      byte[] gcFam = Bytes.toBytes("gc");
      varResult.setGCMin(result.getValue(gcFam, Bytes.toBytes("min")));
      varResult.setGCMax(result.getValue(gcFam, Bytes.toBytes("max")));

      return varResult;
      }
    return null;
    }
  }
