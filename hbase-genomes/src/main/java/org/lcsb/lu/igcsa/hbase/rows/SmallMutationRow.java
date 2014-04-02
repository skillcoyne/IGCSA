/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

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
    if (genome == null || sm == null)
      throw new IllegalArgumentException("Genome name and Variation object required. " + this.toString() );

    log.debug("Added genome info " + genome + " " + sm.getVariationName());

    String family = "info";
    this.addColumn(new Column(family, "genome", genome));
    this.addColumn(new Column(family, "mutation", sm.getVariationName()));

    this.genome = genome;
    }


  public void addLocation(String chr, long segment, long start, long end)
    {
//    if (chr== null || segment  <= 0 || start <=  0 || end <= 0)
//      throw new IllegalArgumentException("Chromosome required for " + this.toString());

    log.debug("addLocation: " + chr + " " + segment + " " + start + "-" + end);

    String family = "loc";
    //"segment", "chr", "start", "end"
    this.addColumn(new Column(family, "segment", segment));
    this.addColumn(new Column(family, "chr", chr));
    this.addColumn(new Column(family, "start", start));
    this.addColumn(new Column(family, "end", end));

    this.segment = segment;
    this.start = start;
    this.chr = chr;
    }

  public void addSequence(String seq)
    {
    if (seq == null)
      seq = " ";
    log.debug("Add seq " + seq);

    this.addColumn(new Column("bp", "seq", seq));
    }

  @Override
  public boolean isRowIdCorrect()
    {
    String testRow = createRowId(genome, chr, segment, start);

    return this.getRowIdAsString().substring(SequenceRow.numRandChars, this.getRowIdAsString().length()).equals(testRow.substring(SequenceRow.numRandChars, testRow.length()));
    }
  }
