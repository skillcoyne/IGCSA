/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.rows.SmallMutationRow;
import org.lcsb.lu.igcsa.hbase.tables.*;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseSequence extends HBaseConnectedObjects
  {
  private static final Log log = LogFactory.getLog(HBaseSequence.class);

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

  @Override
  public void closeTables() throws IOException
    {
    for (AbstractTable t: new AbstractTable[]{sT, smT})
      t.close();
    }


  public boolean addSmallMutation(Variation mutation, int start, int end, String mutationSequence) throws IOException
    {
    if (end < start || mutation == null)
      throw new IllegalArgumentException("The end must be >= start and mutation cannot be null");

    String smRowId = SmallMutationRow.createRowId(this.sequence.getGenome(), this.sequence.getChr(), this.sequence.getSegmentNum(), start );
    if (smT.queryTable(smRowId) != null)
      log.warn("Row " + smRowId + " exists in mutations table, not overwriting");
    else
      {
      SmallMutationRow row = new SmallMutationRow(smRowId);
      row.addGenomeInfo(this.sequence.getGenome(), mutation);
      row.addLocation(this.sequence.getChr(), this.sequence.getStart(), start, end);
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
