/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class KaryotypeResult extends AbstractResult
  {
  static Logger log = Logger.getLogger(KaryotypeResult.class.getName());

  private String genome;
  private String abrType;
  private List<String[]> aberrationDefs = new ArrayList<String[]>();

  protected KaryotypeResult(byte[] rowId)
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

  public List<String[]> getAberrationDefs()
    {
    return aberrationDefs;
    }

  public void addAberrationDefs(byte[] chr, byte[] loc)
    {
    this.aberrationDefs.add(new String[]{Bytes.toString(chr), Bytes.toString(loc)});
    }
  }
