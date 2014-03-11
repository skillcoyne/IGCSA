/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

public class SmallMutationRow extends Row
  {
  private String genome;
  private String chr;
  private long segment = 0;
  private long start = 0;

  public static String createRowId(String genome, String chr, long segment, long startLoc)
    {
    return SequenceRow.createRowId(genome, chr, segment) + "-" + startLoc;
    }

  public SmallMutationRow(String rowId)
    {
    super(rowId);
    }

  public void addGenomeInfo(String genome, Variation sm)
    {
    this.addColumn(new Column("info", "genome", genome));
    this.addColumn(new Column("info", "mutation", sm.getVariationName()));

    this.genome = genome;
    }


  public void addLocation(String chr, long segment, long start, long end)
    {
    this.addColumn( new Column("loc", "segment", segment));

    this.addColumn( new Column("loc", "chr", chr) );
    this.addColumn( new Column("loc", "start", start) );
    this.addColumn( new Column("loc", "end", end) );

    this.segment = segment;
    this.start = start;
    this.chr = chr;
    }

  public void addSequence(String seq)
    {
    if (seq != null)

      this.addColumn(new Column("bp", "seq", seq));
    }

  @Override
  public boolean isRowIdCorrect()
    {
    String testRow = createRowId(genome, chr, segment, start);
    //return (this.getRowIdAsString().substring(numRandChars, this.getRowIdAsString().length()).equals(testRow.substring(numRandChars, testRow.length())) );

    return this.getRowIdAsString().substring(SequenceRow.numRandChars, this.getRowIdAsString().length()).equals(testRow.substring(SequenceRow.numRandChars, testRow.length()));

 //   return (this.genome == null || this.chr == null || this.segment <= 0 || this.start < 0 || !this.getRowIdAsString().equals(createRowId(genome, chr, chrLoc, start)))? false: true;
    }
  }
