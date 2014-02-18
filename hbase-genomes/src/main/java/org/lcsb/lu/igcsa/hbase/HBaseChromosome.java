/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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


  public Put add(long start, long end, String sequence, long segmentNum) throws IOException
    {
    if (end <= start || sequence.length() <=0 )
      throw new IllegalArgumentException("End location must be greater than start, segment must be between 1-" + this.chromosome.getSegmentNumber() + ", and the sequence must have a minimum length of 1");

    String sequenceId = SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), start);
    if (sT.queryTable(sequenceId) != null)
      log.warn("Sequence "+ sequenceId + " already exists. Not overwriting.");
    else
      {
      SequenceRow row = new SequenceRow( sequenceId );
      row.addBasePairs(sequence);
      row.addLocation(this.chromosome.getChrName(), start, end, segmentNum);
      row.addGenome(this.chromosome.getGenomeName());

      return sT.getPut(row);
      }
    return null;
    }

  public HBaseSequence addSequence(long start, long end, String sequence, long segmentNum) throws IOException
    {
    if (!(end >= start || sequence.length() >=0)  )
      throw new IllegalArgumentException("End location must be greater than start, segment must be > 0, and the sequence must have a minimum length of 1 (" + start + "," + end + "," + sequence.length() + ")");

    String sequenceId = SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), segmentNum);
    if (sT.queryTable(sequenceId) != null)
      log.warn("Sequence "+ sequenceId + " already exists. Not overwriting.");
    else
      {
      SequenceRow row = new SequenceRow( sequenceId );
      row.addBasePairs(sequence);
      row.addLocation(this.chromosome.getChrName(), start, end, segmentNum);
      row.addGenome(this.chromosome.getGenomeName());

      sT.addRow(row);
      }

    return new HBaseSequence( sT.queryTable(sequenceId) );
    }

  public HBaseSequence getSequence(long segmentNumber) throws IOException
    {
    SequenceResult result = this.sT.queryTable(SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), segmentNumber));
    return (result == null)? null: new HBaseSequence(result);
    }

  public HBaseSequence getSequenceByStart(long startLocation) throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getGenomeName())));
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getChrName())) );
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("loc"), Bytes.toBytes("start"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(startLocation)) );

    Iterator<Result> rI = this.sT.getScanner(filters);
    Result r = rI.next();
    SequenceResult result = this.sT.createResult(r);

    if (rI.hasNext())
      log.warn("Multiple results for " + startLocation + " returning only the first one.");

    if (result != null)
      return new HBaseSequence(result);

//    if ( chromosome.getSegmentNumber() >= startLocation )
//      {
//      String sequenceId = SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), startLocation);
//      return new HBaseSequence( sT.queryTable(sequenceId) );
//      }
    return null;
    }


  public List<String> getSequenceRowIds(long startLoc, long endLoc) throws IOException
    {
    List<String> rowIds = new LinkedList<String>();
    Iterator<Result> rI = getSequences(startLoc, endLoc);
    while (rI.hasNext())
      rowIds.add( Bytes.toString(rI.next().getRow()) );
    return rowIds;
    }


  public Iterator<Result> getSequences(long startLoc, long endLoc) throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getGenomeName())));
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getChrName())) );
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("loc"), Bytes.toBytes("start"), CompareFilter.CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(startLoc)));
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("loc"), Bytes.toBytes("end"), CompareFilter.CompareOp.LESS_OR_EQUAL, Bytes.toBytes(endLoc)));

    return this.sT.getScanner(filters);
    }

  public Iterator<Result> getSequences() throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getGenomeName())));
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getChrName())) );

    return this.sT.getScanner(filters);
    }


  public int sequenceCount() throws IOException
    {
    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getGenomeName())));
    filters.addFilter( new SingleColumnValueFilter(
        Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(chromosome.getChrName())) );

    return 0;
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
  }
