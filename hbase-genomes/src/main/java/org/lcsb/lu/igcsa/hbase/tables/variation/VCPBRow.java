/**
 * org.lcsb.lu.igcsa.hbase.tables.variation
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.variation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lcsb.lu.igcsa.dist.RandomRange;
import org.lcsb.lu.igcsa.hbase.rows.Row;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.util.Random;


public class VCPBRow extends Row
  {
  private static final Log log = LogFactory.getLog(VCPBRow.class);

  private static final char sep = '_';

  private String chr, variationName;
  private int gcMin, gcMax;

  private static int rowFrag = 1;

//  private static int generateRandom()
//    {
//    int rand = (int) (System.currentTimeMillis() + new Random(randSeed).nextLong());
//    return rand;
//    }

  public static void resetRowCounter()
    {
    rowFrag = 1;
    }

  public static void incrementRowCounter()
    {
    ++rowFrag;
    }

  public static int getRowCounter()
    {
    return rowFrag;
    }

  public static String createRowId(String chr, String variation, int min, int max, int fragmentNumber)
    {
    String rowId = String.valueOf(fragmentNumber) + sep + chr + sep + variation + sep + min + "-" + max;
    return rowId;
    }

  public static String createRowId(String chr, String variation, int min, int max)
    {
    if (rowFrag <= 1)
      log.info("foo");
    String rowId = String.valueOf(rowFrag) + sep + chr + sep + variation + sep + min + "-" + max;
    return rowId;
    }

  public VCPBRow(String rowId)
    {
    super(rowId);
    }

  public void addChromosome(String chr)
    {
    super.addColumn( new Column("chr", "name", chr));

    this.chr = chr;
    }

  public void addVariation(String name, String varClass, int varCount)
    {
    String family = "var";
    super.addColumn( new Column(family, "name", name) );
    super.addColumn( new Column(family, "count", varCount) );
    super.addColumn( new Column(family, "class", varClass) );

    this.variationName = name;
    }


  public void addGCRange(int gcMin, int gcMax, int fragmentNum)
    {
    String family = "gc";
    super.addColumn( new Column(family, "min", gcMin) );
    super.addColumn( new Column(family, "max", gcMax) );
    super.addColumn( new Column(family, "frag", fragmentNum) );

    this.gcMin = gcMin;
    this.gcMax = gcMax;
    }

  @Override
  public boolean isRowIdCorrect()
    {
    String testRow = createRowId(chr, variationName, gcMin, gcMax);
    return testRow.substring(testRow.indexOf("_"), testRow.length()).equals(this.getRowIdAsString().substring(this.getRowIdAsString().indexOf("_"), this.getRowIdAsString().length()));
    }
  }
