/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.rows.Row;
import org.lcsb.lu.igcsa.hbase.rows.SmallMutationRow;
import org.lcsb.lu.igcsa.hbase.tables.AbstractTable;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.genomes.SequenceResult;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.io.IOException;
import java.util.*;

public class SmallMutationsTable extends AbstractTable<SmallMutationsTable>
  {
  public SmallMutationsTable(Configuration configuration, String tableName) throws IOException
    {
    super(configuration, tableName);
    }

  public Row newMutationRow(SequenceResult sequence, Variation mutation, int start, int end, String mutationSequence) throws IOException
    {
    if (end < start || mutation == null)
      throw new IllegalArgumentException("The end must be >= start and mutation cannot be null");

    String smRowId = SmallMutationRow.createRowId(sequence.getGenome(), sequence.getChr(), sequence.getSegmentNum(), start);

    SmallMutationRow row = new SmallMutationRow(smRowId);
    row.addGenomeInfo(sequence.getGenome(), mutation);
    row.addLocation(sequence.getChr(), sequence.getSegmentNum(), start, end);
    row.addSequence(mutationSequence);

    return row;
    }


  public String addMutation(SequenceResult sequence, Variation mutation, int start, int end, String mutationSequence) throws IOException
    {
    Row row = newMutationRow(sequence, mutation, start, end, mutationSequence);

    if (this.queryTable(row.getRowIdAsString()) != null)
      log.warn("Row " + row.getRowIdAsString() + " exists in mutations table, OVERWRITING!");
    else
      {
      try
        {
        this.addRow(row);
        }
      catch (IOException ioe)
        {
        log.error(ioe);
        return null;
        }
      }
    return row.getRowIdAsString();
    }

  @Override
  public SmallMutationsResult queryTable(String rowId, Column column) throws IOException
    {
    return createResult((Result) super.queryTable(rowId, column));
    }

  @Override
  public SmallMutationsResult queryTable(String rowId) throws IOException
    {
    return createResult((Result) super.queryTable(rowId));
    }

  @Override
  public List<SmallMutationsResult> getRows() throws IOException
    {
    return createResults((List<Result>) super.getRows());
    }

  @Override
  public List<SmallMutationsResult> queryTable(Column... columns) throws IOException
    {
    return createResults((List<Result>) super.queryTable(columns));
    }

  @Override
  protected List<SmallMutationsResult> createResults(List<Result> results)
    {
    List<SmallMutationsResult> mutResults = new ArrayList<SmallMutationsResult>();
    for (Result r : results)
      mutResults.add(createResult(r));
    return mutResults;
    }

  @Override
  protected SmallMutationsResult createResult(Result result)
    {
    if (result.getRow() != null)
      {
      SmallMutationsResult mutResult = new SmallMutationsResult(result.getRow());

      for (KeyValue kv : result.list())
        {
        String family = Bytes.toString(kv.getFamily());
        String qualifier = Bytes.toString(kv.getQualifier());
        byte[] value = kv.getValue();

        if (family.equals("info"))
          {
          if (qualifier.equals("genome"))
            mutResult.setGenome(value);
          else if (qualifier.equals("mutation"))
            mutResult.setMutation(value);
          }
        else if (family.equals("loc"))
          {
          if (qualifier.equals("segment"))
            mutResult.setSegment(value);
          else if (qualifier.equals("chr"))
            mutResult.setChr(value);
          else if (qualifier.equals("start"))
            mutResult.setStart(value);
          else if (qualifier.equals("end"))
            mutResult.setEnd(value);
          }
        else if (family.equals("bp"))
          mutResult.setSequence(value);
        }

      return mutResult;
      }
    return null;
    }
  }
