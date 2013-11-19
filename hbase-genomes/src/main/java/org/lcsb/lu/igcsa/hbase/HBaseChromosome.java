/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseChromosome extends HBaseConnectedObjects
  {
  static Logger log = Logger.getLogger(HBaseChromosome.class.getName());

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

  public HBaseSequence addSequence(int start, int end, int segmentNumber, String sequence) throws IOException
    {
    if (end <= start || sequence.length() <=0 || (segmentNumber > 0 && segmentNumber <= this.chromosome.getSegmentNumber()))
      throw new IllegalArgumentException("End location must be greater than start, segment must be between 1-" + this.chromosome.getSegmentNumber() + ", and the sequence must have a minimum length of 1");

    String sequenceId = SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), segmentNumber);
    if (sT.queryTable(sequenceId) != null)
      log.warn("Sequence "+ sequenceId + " already exists. Not overwriting.");
    else
      {
      SequenceRow row = new SequenceRow( sequenceId );
      row.addBasePairs(sequence);
      row.addLocation(this.chromosome.getChrName(), segmentNumber, start, end);
      row.addGenome(this.chromosome.getGenomeName());

      sT.addRow(row);
      }

    return new HBaseSequence( sT.queryTable(sequenceId) );
    }

  public HBaseSequence getSequence(int segmentNumber) throws IOException
    {
    if ( chromosome.getSegmentNumber() >= segmentNumber )
      {
      String sequenceId = SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), segmentNumber);
      return new HBaseSequence( sT.queryTable(sequenceId) );
      }
    return null;
    }

  // TODO This could be done with HBase filters, might be faster
  public List<HBaseSequence> getSequences(int startLoc, int endLoc) throws IOException
    {
    List<HBaseSequence> sequences = new ArrayList<HBaseSequence>();
    for (int i=1; i<=this.chromosome.getSegmentNumber(); i++)
      {
      SequenceResult currResult = this.sT.queryTable(SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), i));
      IntRange resultRange = new IntRange(currResult.getStart(), currResult.getEnd());
      if (resultRange.containsRange( new IntRange(startLoc, endLoc)) )
        sequences.add(new HBaseSequence(currResult));
      }

    return sequences;
    }


  // TODO Again, there's probably a better way to do this with a filter but...
//  public List<HBaseSequence> getAllSequences() throws IOException
//    {
//    List<HBaseSequence> sequences = new ArrayList<HBaseSequence>();
//    for (int i=1; i<=this.chromosome.getSegmentNumber(); i++)
//      sequences.add(new HBaseSequence(this.sT.queryTable(SequenceRow.createRowId(this.chromosome.getGenomeName(), this.chromosome.getChrName(), i))));
//
//    return sequences;
//    }


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
