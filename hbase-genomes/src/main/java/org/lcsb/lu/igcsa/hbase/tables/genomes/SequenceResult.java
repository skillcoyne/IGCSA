/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;
import org.springframework.util.StringUtils;


public class SequenceResult extends AbstractResult
  {
  private String sequence;
  private String chr;
  private String genome;

  private long start;
  private long end;

  private long segmentNum;

  public long getSegmentNum()
    {
    return segmentNum;
    }

  public void setSegmentNum(byte[] segmentNum)
    {
    this.segmentNum = Bytes.toLong(segmentNum);
    }

  protected SequenceResult(byte[] rowId)
    {
    super(rowId);
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

  public String getGenome()
    {
    return genome;
    }

  public void setGenome(byte[] genome)
    {
    this.genome = Bytes.toString(genome);
    }

  public long getStart()
    {
    return start;
    }

  public void setStart(byte[] start)
    {
    this.start = Bytes.toLong(start);
    }

  public long getEnd()
    {
    return end;
    }

  public void setEnd(byte[] end)
    {
    this.end = Bytes.toLong(end);
    }

  public int getGC()
    {
    int guanine = StringUtils.countOccurrencesOf(sequence, "G");
    int cytosine = StringUtils.countOccurrencesOf(sequence, "C");
    return guanine+cytosine;
    }

  public long getSequenceLength()
    {
    return sequence.length();
    }

  @Override
  public String toString()
    {
    return SequenceRow.createRowId(genome, chr, segmentNum);
    }
  }
