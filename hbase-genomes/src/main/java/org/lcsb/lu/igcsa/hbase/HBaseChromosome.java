/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.*;

public class HBaseChromosome extends HBaseConnectedObjects
  {
  private static final Log log = LogFactory.getLog(HBaseChromosome.class);

  private ChromosomeResult chromosome;

  private ChromosomeTable cT;
  private SequenceTable sT;

  protected HBaseChromosome(AbstractResult result) throws IOException
    {
    super(result);
    this.chromosome = (ChromosomeResult) result;
    }

  protected HBaseChromosome(String rowId) throws IOException
    {
    super(rowId);
    this.chromosome = cT.queryTable(rowId);
    }

  public boolean addSequence(long start, long end, String sequence, long segmentNum, boolean check) throws IOException
    {
    if (!(end >= start || sequence.length() >= 0))
      throw new IllegalArgumentException("End location must be greater than start, segment must be > 0, " +
                                         "and the sequence must have a minimum length of 1 (" + start + "," + end + "," +
                                         "" + sequence.length() + ")");
    // this is VERY slow
    if (check)
      {
      if (this.getSequence(segmentNum) != null)
        throw new IOException("Sequence for " + chromosome.getGenomeName() + " " +
                                                                      chromosome.getChrName() + " " + segmentNum + " already exists. Not " +
                                                                      "overwriting.");
      }

    SequenceRow row = new SequenceRow(SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), segmentNum));
    row.addBasePairs(sequence);
    row.addLocation(this.chromosome.getChrName(), start, end, segmentNum);
    row.addGenome(this.chromosome.getGenomeName());

    try
      {
      sT.addRow(row);
      log.info("");
      }
    catch (IOException e)
      {
      return false;
      }

    return true;
    }

  public HBaseSequence getSequence(long segmentNumber) throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chromosome.getGenomeName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chromosome.getChrName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("segment"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(segmentNumber)));

    Iterator<Result> rI = this.sT.getScanner(filters);
    Result r = rI.next();

    if (rI.hasNext()) log.warn("Multiple results for segment " + segmentNumber + " returning only the first one.");

    return (r == null) ? null : new HBaseSequence(this.sT.createResult(r));
    }

  public HBaseSequence getSequenceByStart(long startLocation) throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chromosome.getGenomeName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chromosome.getChrName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("start"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(startLocation)));

    Iterator<Result> rI = this.sT.getScanner(filters);
    Result r = rI.next();
    SequenceResult result = this.sT.createResult(r);

    if (rI.hasNext()) log.warn("Multiple results for " + startLocation + " returning only the first one.");

    if (result != null) return new HBaseSequence(result);

    return null;
    }


  public List<String> getSequenceRowIds(long startLoc, long endLoc) throws IOException
    {
    List<String> rowIds = new LinkedList<String>();
    Iterator<Result> rI = this.getSequences(startLoc, endLoc);
    while (rI.hasNext()) rowIds.add(Bytes.toString(rI.next().getRow()));

    Collections.sort(rowIds, new SequenceRowIdComparator());

    return rowIds;
    }


  public Iterator<Result> getSequences(long startLoc, long endLoc) throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chromosome.getGenomeName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL,
                                                  Bytes.toBytes(chromosome.getChrName())));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("start"), CompareFilter.CompareOp.GREATER_OR_EQUAL,
                                                  Bytes.toBytes(startLoc)));
    filters.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("end"), CompareFilter.CompareOp.LESS_OR_EQUAL,
                                                  Bytes.toBytes(endLoc)));

    return this.sT.getScanner(filters);
    }


  public ChromosomeResult getChromosome()
    {
    return this.chromosome;
    }

  @Override
  protected void getTables() throws IOException
    {
    this.cT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getChromosomeTable();
    this.sT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable();
    }

  private class SequenceRowIdComparator implements Comparator<String>
    {
    private int parse(String rowId)
      {
      rowId = rowId.substring(2, rowId.indexOf(":"));
      return Integer.parseInt(rowId);
      }

    @Override
    public int compare(String s1, String s2)
      {
      if (s1.equals(s2)) return 0;
      return (parse(s1) > parse(s2)) ? 1 : -1;
      }
    }

  }
