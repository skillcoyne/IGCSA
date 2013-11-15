/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.Column;

public class SmallMutationRow extends Row
  {
  static Logger log = Logger.getLogger(SmallMutationRow.class.getName());

  public static enum SmallMutation
    {
    SNV("SNV"), DEL("deletion"), INS("insertion"), INDEL("indel"), SUB("substitution");

    private String type;
    private SmallMutation(String type)
      {
      this.type = type;
      }

    public String getType()
      {
      return this.type;
      }
    }

  private String genome;
  private String chr;
  private int segment = 0;
  private int start = -1;

  public static String createRowId(String genome, String chr, int seg, int startLoc)
    {
    return SequenceRow.createRowId(genome, chr, seg) + "-" + startLoc;
    }

  public SmallMutationRow(String rowId)
    {
    super(rowId);
    }

  public void addGenomeInfo(String genome, SmallMutation sm)
    {
    this.addColumn(new Column("info", "genome", genome));
    this.addColumn(new Column("info", "mutation", sm.getType()));

    this.genome = genome;
    }


  public void addLocation(String chr, int segment, int start, int end)
    {
    this.addColumn( new Column("loc", "chr", chr) );
    this.addColumn( new Column("loc", "segment", segment) );
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
    return (this.genome == null || this.chr == null || this.segment <= 0 || this.start < 0 || !this.getRowIdAsString().equals(createRowId(genome, chr, segment, start)))? false: true;
    }
  }
