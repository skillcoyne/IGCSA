/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;

import java.util.ArrayList;
import java.util.List;

public class AberrationResult extends AbstractResult
  {
  private String genome;
  private String abrType;

  private List<Location> aberrationDefs = new ArrayList<Location>();

  protected AberrationResult(byte[] rowId)
    {
    super(rowId);
    }

  public String getGenome()
    {
    return genome;
    }

  public void setGenome(byte[] genome)
    {
    this.genome = Bytes.toString(genome);
    }

  public String getAbrType()
    {
    return abrType;
    }

  public void setAbrType(byte[] abrType)
    {
    this.abrType = Bytes.toString(abrType);
    }

  public List<Location> getAberrationDefinitions()
    {
    return aberrationDefs;
    }

  public void addAberrationDefinitions(byte[] chr, byte[] loc)
    {
    String chromosome = Bytes.toString(chr);
    String[] locationDef = Bytes.toString(loc).split("-");

    aberrationDefs.add(new Location(chromosome, Integer.parseInt(locationDef[0]), Integer.parseInt(locationDef[1])));
    }

  }
