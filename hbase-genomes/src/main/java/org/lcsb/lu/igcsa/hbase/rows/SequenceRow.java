/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.util.Random;

public class SequenceRow extends Row
  {
  private String genome;
  private String chr;
  private long segmentNum;

  private static  final int numRandChars = 4;

  // This means that I can never deterministically guess what the row id is for any given sequence.  All queries will have to be run on a column based search
  private static String generateRandom(String chr)
    {
    StringBuffer rstr = new StringBuffer();

    RandomRange rand = new RandomRange(65, 90);
    for (int i=0; i<numRandChars; i++)
      rstr.append((char) rand.nextInt());

    return rstr.toString();
    }

  public static String createRowId(String genome, String chr, long segmentNum)
    {
    //1000000000
    if (segmentNum > 99999999)
      throw new RuntimeException("Overran formatted size, 8d is not enough.");

    String formattedSeq = String.format("%08d", segmentNum);
    return  generateRandom(chr) + formattedSeq + ":" + ChromosomeRow.createRowId(genome, chr);
    }

  public SequenceRow(String rowId)
    {
    super(rowId);
    }

  public void addBasePairs(String sequence)
    {
    super.addColumn( new Column("bp", "seq", sequence));
    }

  public void addLocation(String chr, long start, long end, long segmentNum)
    {
    super.addColumn( new Column("loc", "chr", chr) );
    super.addColumn( new Column("loc", "start", start) );
    super.addColumn( new Column("loc", "end", end) );
    super.addColumn( new Column("loc", "segment", segmentNum) );

    this.chr = chr;
    this.segmentNum = segmentNum;
    }

  public void addGenome(String name)
    {
    super.addColumn( new Column("info", "genome", name));
    this.genome = name;
    }

  @Override
  public boolean isRowIdCorrect()
    {
    if (chr == null || segmentNum <= 0 || genome == null )
      return false;

    String testRow = createRowId(genome, chr, segmentNum);
    return (this.getRowIdAsString().substring(numRandChars, this.getRowIdAsString().length()).equals(testRow.substring(numRandChars, testRow.length())) );
    }

  }
