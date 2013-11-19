/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.SmallMutationRow;
import org.lcsb.lu.igcsa.hbase.tables.*;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.io.IOException;

public class HBaseSequence extends HBaseConnectedObjects
  {
  static Logger log = Logger.getLogger(HBaseSequence.class.getName());

  private SequenceTable sT;
  private SmallMutationsTable smT;

  private SequenceResult sequence;

  public HBaseSequence(AbstractResult result) throws IOException
    {
    super(result);
    this.sequence = (SequenceResult) result;
    }

  public HBaseSequence(String rowId) throws IOException
    {
    super(rowId);
    this.sequence = sT.queryTable(rowId);
    }


  public boolean addSmallMutation(Variation mutation, int start, int end, String mutationSequence) throws IOException
    {
    if (end < start || mutation == null)
      throw new IllegalArgumentException("The end must be >= start and mutation cannot be null");

    String smRowId = SmallMutationRow.createRowId(this.sequence.getGenome(), this.sequence.getChr(), this.sequence.getSegment(), start );
    if (smT.queryTable(smRowId) != null)
      log.warn("Row " + smRowId + " exists in mutations table, not overwriting");
    else
      {
      SmallMutationRow row = new SmallMutationRow(smRowId);
      row.addGenomeInfo(this.sequence.getGenome(), mutation);
      row.addLocation(this.sequence.getChr(), this.sequence.getSegment(), start, end);
      row.addSequence(mutationSequence);

      smT.addRow(row);

      return true;
      }

    return false;
    }


  public SequenceResult getSequence()
    {
    return sequence;
    }

  @Override
  protected void getTables() throws IOException
    {
    this.sT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable();
    this.smT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSmallMutationsTable();
    }
  }
