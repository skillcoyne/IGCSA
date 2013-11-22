/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.generator.Aberration;
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
  static Logger log = Logger.getLogger(KaryotypeIndexRow.class.getName());

  private String parentGenome;
  //private String[] abrs;
  private List<Aberration> aberrations;

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


  public Map<String, Aberration> getKaryotypeTableRowIds()
    {
    Map<String, Aberration> karyotypeTableRows = new HashMap<String, Aberration>();
    for (Aberration abr : this.aberrations)
      karyotypeTableRows.put(this.getRowIdAsString() + "-" + abr.getWithLocations(), abr);

    return karyotypeTableRows;
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
