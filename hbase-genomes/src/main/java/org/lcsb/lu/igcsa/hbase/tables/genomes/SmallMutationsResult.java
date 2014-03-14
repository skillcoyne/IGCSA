/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;

import java.io.IOException;
import java.util.List;

public class SmallMutationsResult extends AbstractResult
  {
  static Logger log = Logger.getLogger(SmallMutationsResult.class.getName());

  private String genome;
  private String mutation;
  private String sequence;
  private String chr;

  private int start;
  private int end;
  private int segment;

  protected SmallMutationsResult(byte[] rowId)
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

  public String getMutation()
    {
    return mutation;
    }

  public void setMutation(byte[] mutation)
    {
    this.mutation = Bytes.toString(mutation);
    }

  public String getSequence()
    {
    return sequence;
    }

  public void setSequence(byte[] sequence)
    {
    this.sequence = Bytes.toString(sequence);
    }

  public String getChr()
    {
    return chr;
    }

  public void setChr(byte[] chr)
    {
    this.chr = Bytes.toString(chr);
    }

  public int getStart()
    {
    return start;
    }

  public void setStart(byte[] start)
    {
    this.start = Bytes.toInt(start);
    }

  public int getEnd()
    {
    return end;
    }

  public void setEnd(byte[] end)
    {
    this.end = Bytes.toInt(end);
    }

  public int getSegment()
    {
    return segment;
    }

  public void setSegment(byte[] segment)
    {
    this.segment = Bytes.toInt(segment);
    }
  }
