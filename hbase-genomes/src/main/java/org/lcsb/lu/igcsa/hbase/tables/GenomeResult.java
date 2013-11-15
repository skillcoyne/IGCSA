/**
 * org.lcsb.lu.igcsa.hbase.tables.results
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenomeResult extends AbstractResult
  {
  private String name;
  private String parent;
  private List<String> chromosomes;

  protected GenomeResult(byte[] rowId)
    {
    super(rowId);
    this.chromosomes = new ArrayList<String>();
    }

  @Override
  public List<ChromosomeResult> getAssociatedResults(String rowId, AbstractTable connectedTable) throws IOException
    {
    ChromosomeTable chrTable = (ChromosomeTable) connectedTable;
    List<ChromosomeResult> chromosomesList = new ArrayList<ChromosomeResult>();
    for (String chr: this.getChromosomes())
      chrTable.queryTable(ChromosomeRow.createRowId(this.getName(), chr));

    return chromosomesList;
    }


  protected GenomeResult(byte[] rowId, byte[] name, byte[] parent, byte[] chromosomes)
    {
    super(rowId);

    setName(name);
    setParent(parent);
    setChromosomes(chromosomes);
    }

  protected void setName(byte[] name)
    {
    this.name = Bytes.toString(name);
    }

  protected void setParent(byte[] parent)
    {
    this.parent = Bytes.toString(parent);
    }

  protected void setChromosomes(byte[] chromosomes)
    {
    String chrs = Bytes.toString(chromosomes);
    for (String c : chrs.split(","))
      this.chromosomes.add(c);
    }

  public String getName()
    {
    return name;
    }

  public String getParent()
    {
    return parent;
    }

  public List<String> getChromosomes()
    {
    return chromosomes;
    }

  }
