/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.Aneuploidy;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KaryotypeIndexRow extends Row
  {
  private String parentGenome;
  //private String[] abrs;
  private List<Aberration> aberrations;
  private List<Aneuploidy> aneuploidies;

  public KaryotypeIndexRow(String rowId, String parentGenome)
    {
    super(rowId);
    this.addColumn(new Column("info", "genome", parentGenome));
    this.parentGenome = parentGenome;
    }

  public void addAberrations(List<Aberration> abrs) throws IOException
    {
    int i = 1;
    for (Aberration abr : abrs)
      {
      checkAberrationFormat(abr.getWithLocations());
      this.addColumn(new Column("abr", Integer.toString(i), abr.getWithLocations()));
      ++i;
      }
    this.aberrations = abrs;
    }


  public void addAneuploidies(List<Aneuploidy> aps) throws IOException
    {
    int i = 1;
    for (Aneuploidy ap: aps)
      {
      if (ap.getGain() > 0)
        addColumn( new Column("gain", "chr"+i, ap.getChromosome()+"("+ ap.getGain()+")") );
      if (ap.getLoss() > 0)
        addColumn( new Column("loss", "chr"+i, ap.getChromosome()+"("+ ap.getLoss()+")") );
      ++i;
      }
    this.aneuploidies = aps;
    }


  public Map<String, Aberration> getAberrationRowIds()
    {
    Map<String, Aberration> aberrationTableRows = new HashMap<String, Aberration>();
    for (Aberration abr : this.aberrations)
      aberrationTableRows.put(this.getRowIdAsString() + "-" + abr.getWithLocations(), abr);

    return aberrationTableRows;
    }


  private void checkAberrationFormat(String abr) throws IOException
    {
    Pattern p = Pattern.compile("^\\w+\\((\\d+|X|Y):\\d+-\\d+(,\\s?(\\d+|X|Y):\\d+-\\d+){0,}\\)$");
    Matcher m = p.matcher(abr);
    boolean formatOk = m.matches();

    if (!formatOk)
      throw new IOException("Aberration format incorrect. Expected  <characters>(chr:loc-loc), got " + abr);

    }

  @Override
  public boolean isRowIdCorrect()
    {
    return true;
    }
  }
