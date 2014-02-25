/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;

import java.io.IOException;
import java.util.*;

public class SequenceTable extends AbstractTable
  {

  public SequenceTable(Configuration configuration, String tableName) throws IOException
    {
    super(configuration, tableName);
    }


  public String addSequence(ChromosomeResult chr, long start, long end, String sequence, long segmentNum) throws IOException
    {
    if (!(end >= start || sequence.length() >= 0))
      throw new IllegalArgumentException("End location must be greater than start, segment must be > 0, " +
                                         "and the sequence must have a minimum length of 1 (" + start + "," + end + "," +
                                         "" + sequence.length() + ")");

    SequenceRow row = new SequenceRow(SequenceRow.createRowId(chr.getGenomeName(), chr.getChrName(), segmentNum));
    row.addBasePairs(sequence);
    row.addLocation(chr.getChrName(), start, end, segmentNum);
    row.addGenome(chr.getGenomeName());

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

  public SequenceResult getSequenceFor(ChromosomeResult chr, long segment) throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL,
        Bytes.toBytes(chr.getGenomeName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL,
        Bytes.toBytes(chr.getChrName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("segment"), CompareFilter.CompareOp.EQUAL,
        Bytes.toBytes(segment)));

    Scan scan = new Scan();
    scan.setFilter(filters);

    ResultScanner scanner = this.getScanner(scan);
    Iterator<Result> rI = scanner.iterator();

    SequenceResult sequence = this.createResult(rI.next());
    if (rI.hasNext())
      log.warn("Multiple results for sequence segment query, returning only the first one.");

    return sequence;
    }

  @Override
  public SequenceResult queryTable(String rowId, Column column) throws IOException
    {
    return createResult((Result) super.queryTable(rowId, column));
    }

  @Override
  public SequenceResult queryTable(String rowId) throws IOException
    {
    return createResult((Result) super.queryTable(rowId));
    }

  @Override
  public List<SequenceResult> getRows() throws IOException
    {
    return createResults((List<Result>) super.getRows());
    }

  @Override
  public List<SequenceResult> queryTable(Column... columns) throws IOException
    {
    return createResults((List<Result>) super.queryTable(columns));
    }

  @Override
  protected List<SequenceResult> createResults(List<Result> results)
    {
    List<SequenceResult> sequenceResults = new ArrayList<SequenceResult>();
    for (Result r : results)
      sequenceResults.add(createResult(r));

    return sequenceResults;
    }

  @Override
  public SequenceResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      SequenceResult seqResult = new SequenceResult(result.getRow());

      for (KeyValue kv : result.list())
        {
        String family = Bytes.toString(kv.getFamily());
        String qualifier = Bytes.toString(kv.getQualifier());
        byte[] value = kv.getValue();

        if (family.equals("info") && qualifier.equals("genome")) seqResult.setGenome(value);

        if (family.equals("bp") && qualifier.equals("seq")) seqResult.setSequence(value);

        if (family.equals("loc"))
          {
          if (qualifier.equals("start")) seqResult.setStart(value);
          else if (qualifier.equals("end")) seqResult.setEnd(value);
          else if (qualifier.equals("chr")) seqResult.setChr(value);
          else if (qualifier.equals("segment")) seqResult.setSegmentNum(value);
          }
        }

      return seqResult;
      }

    return null;
    }

  public Iterator<Result> getSequencesFor(String genome, String chr, long fromLoc, long toLoc) throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(genome)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chr)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("start"), CompareFilter.CompareOp.GREATER_OR_EQUAL,
                                                  Bytes.toBytes(fromLoc)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("end"), CompareFilter.CompareOp.LESS_OR_EQUAL,
                                                  Bytes.toBytes(toLoc)));

    Scan scan = new Scan();
    scan.setFilter(filters);

    ResultScanner scanner = this.getScanner(scan);

    return scanner.iterator();
    }

  }

