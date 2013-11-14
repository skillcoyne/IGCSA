/**
 * org.lcsb.lu.igcsa.hbase.rows
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.rows;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.Column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KaryotypeIndexRow extends Row
  {
  static Logger log = Logger.getLogger(KaryotypeIndexRow.class.getName());

  private String genome;
  private String[] abrs;

  public KaryotypeIndexRow(String rowId)
    {
    super(rowId);
    this.addColumn(new Column("info", "genome", rowId));
    }

  public void addAberrations(String... abrs) throws IOException
    {
    for (int i=0; i<abrs.length; i++)
      {
      checkAberrationFormat(abrs[i]);
      this.addColumn( new Column("abr", Integer.toString(i+1), abrs[i]) );
      }
    this.abrs = abrs;
    }


  public List<String> getKaryotypeTableRowIds()
    {
    List<String> rowIds = new ArrayList<String>();
    for (String abr: abrs)
      rowIds.add( genome + "-" + abr );

    return rowIds;
    }


  private void checkAberrationFormat(String abr) throws IOException
    {
    Pattern p = Pattern.compile("^\\w+\\(((\\d+|X|Y):\\d+-\\d+)((,(\\d+|X|Y):\\d+-\\d+)){0,}\\)$");
    Matcher m = p.matcher(abr);
    boolean formatOk = m.matches();

    if (!formatOk)
      throw new IOException("Aberration format incorrect. Expected  <characters>(chr:loc-loc)");

    }

  @Override
  public boolean isRowIdCorrect()
    {
    return true;
    }
  }
