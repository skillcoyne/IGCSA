/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.rows.SmallMutationRow;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SequenceResult extends AbstractResult
  {
  static Logger log = Logger.getLogger(SequenceResult.class.getName());

  private String sequence;
  private String chr;
  private String genome;

  private int start;
  private int end;
  private int segment;

  protected SequenceResult(byte[] rowId)
    {
    super(rowId);
    }

  @Override
  public List<SmallMutationsResult> getAssociatedResults(String rowId, AbstractTable connectedTable) throws IOException
    {
    SmallMutationsTable smTable = (SmallMutationsTable) connectedTable;

    List<SmallMutationsResult> smallMutations = new ArrayList<SmallMutationsResult>();
    for (int i=start; i<=end; i++)
      smallMutations.add(smTable.queryTable(SmallMutationRow.createRowId(genome, chr, segment, start)));

    return smallMutations;
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

  public int getGC()
    {
    int guanine = StringUtils.countOccurrencesOf(sequence, "G");
    int cytosine = StringUtils.countOccurrencesOf(sequence, "C");
    return guanine+cytosine;
    }

  public int getSequenceLength()
    {
    return sequence.length();
    }



  }
