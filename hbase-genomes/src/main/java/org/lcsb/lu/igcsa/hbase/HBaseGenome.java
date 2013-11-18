/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.rows.GenomeRow;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HBaseGenome extends HBaseConnectedObjects
  {
  static Logger log = Logger.getLogger(HBaseGenome.class.getName());

  private GenomeResult genome;

  private GenomeTable gT;
  private ChromosomeTable cT;


  protected HBaseGenome(GenomeResult result) throws IOException
    {
    super(result);
    this.genome = result;
    }

  protected HBaseGenome(String rowId) throws IOException
    {
    super(rowId);
    this.genome = this.gT.queryTable(rowId);
    }

  public boolean addChromosome(String chr, int length, int numSegments) throws IOException
    {
    GenomeResult result = gT.queryTable(genome.getName(), new Column("chr", "list"));

    List<String> currentChrs = result.getChromosomes();
    if (currentChrs.contains(chr))
      log.warn("Chromosome " + chr + " exists. Not overwriting.");
    else
      {
      currentChrs.add(chr);

      gT.updateRow(genome.getName(), new Column("chr", "list", StringUtils.join(currentChrs.iterator(), ",")));

      ChromosomeRow row = new ChromosomeRow(ChromosomeRow.createRowId(genome.getName(), chr));
      row.addGenome(genome.getName());
      row.addChromosomeInfo(chr, length, numSegments);

      cT.addRow(row);
      return true;
      }

    return false;
    }

  public HBaseChromosome getChromosome(String chr) throws IOException
    {
    ChromosomeResult result = cT.queryTable( ChromosomeRow.createRowId(genome.getName(), chr) );
    return new HBaseChromosome(result);
    }

  @Override
  protected void getTables() throws IOException
    {
    this.gT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getGenomeTable();
    this.cT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getChromosomeTable();
    }
  }
