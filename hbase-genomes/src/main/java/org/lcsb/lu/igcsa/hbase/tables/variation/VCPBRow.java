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
  private static final int randSeed = 1000;

  private String chr, variationName, variationClass;
  private long varCount, gcMin, gcMax;

  private static int generateRandom()
    {
    int rand = (int) (System.currentTimeMillis() + new Random(randSeed).nextLong());
    return rand;
    }

  public static String createRowId(String chr, long count, String variation, long min, long max)
    {
    String rowId = generateRandom() + sep + chr + sep + variation + sep + min + sep + max;
    return rowId;
    }

  public VCPBRow(String rowId)
    {
    super(rowId);
    }

  public VCPBRow(String rowId, Column[] columns)
    {
    super(rowId, columns);
    }

  public void addChromosome(String chr)
    {
    super.addColumn( new Column("chr", "name", chr));

    this.chr = chr;
    }

  public void addVariation(String name, String varClass, long varCount)
    {
    String family = "var";
    super.addColumn( new Column(family, "name", name) );
    super.addColumn( new Column(family, "count", varCount) );
    super.addColumn( new Column(family, "class", varClass) );

    this.variationName = name;
    this.variationClass = varClass;
    this.varCount = varCount;
    }


  public void addGCRange(long gcMin, long gcMax)
    {
    String family = "gc";
    super.addColumn( new Column(family, "minimum", gcMin) );
    super.addColumn( new Column(family, "maximum", gcMax) );

    this.gcMin = gcMin;
    this.gcMax = gcMax;
    }


  @Override
  public boolean isRowIdCorrect()
    {
    String testRow = createRowId(chr, varCount, variationName, gcMin, gcMax);
    return testRow.substring(testRow.indexOf("_"), testRow.length()).equals(this.getRowIdAsString().substring(this.getRowIdAsString().indexOf("_"), this.getRowIdAsString().length()));
    }
  }
