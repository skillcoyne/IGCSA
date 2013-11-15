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

  private List<AberrationLocation> aberrationDefs = new ArrayList<AberrationLocation>();

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

  public List<AberrationLocation> getAberrationDefinitions()
    {
    return aberrationDefs;
    }

  public void addAberrationDefinitions(byte[] chr, byte[] loc)
    {
    String chromosome = Bytes.toString(chr);
    String[] locationDef = Bytes.toString(loc).split(",");

    this.aberrationDefs.add(new AberrationLocation(chromosome, Integer.parseInt(locationDef[0]), Integer.parseInt(locationDef[1])));
    }

  public class AberrationLocation
    {
    private int start;
    private int stop;
    private String chr;

    protected AberrationLocation(String chr, int start, int stop)
      {
      this.start = start;
      this.stop = stop;
      this.chr = chr;
      }

    public int getStart()
      {
      return start;
      }

    public int getStop()
      {
      return stop;
      }

    public String getChr()
      {
      return chr;
      }
    }

  }
