/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables.genomes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.Aneuploidy;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.tables.AbstractResult;

import java.util.ArrayList;
import java.util.List;


public class KaryotypeIndexResult extends AbstractResult
  {
  private String karyotype;
  private String parentGenome;
  //private List<String> abrs;
  private List<Aberration> abrs;
  private List<Aneuploidy> aps;

  KaryotypeIndexResult(byte[] rowId)
    {
    super(rowId);
    this.karyotype = Bytes.toString(rowId);
    //abrs = new ArrayList<String>();
    abrs = new ArrayList<Aberration>();
    aps = new ArrayList<Aneuploidy>();
    }

  public String getKaryotype()
    {
    return karyotype;
    }

  public String getParentGenome()
    {
    return parentGenome;
    }

  public void addParentGenome(byte[] parentGenome)
    {
    this.parentGenome = Bytes.toString(parentGenome);
    }

  public List<Aberration> getAberrations()
    {
    return abrs;
    }
  //    public List<String> getAberrations()
  //      {
  //      return abrs;
  //      }

  public void addAberration(byte[] aberration)
    {
    String abr = Bytes.toString(aberration);
    this.abrs.add(Aberration.parseAberration(abr));
    }

  public List<Aneuploidy> getAneuploidy()
    {
    return aps;
    }

  public void addAneuploidy(byte[] aneuploidy, boolean gain)
    {
    String value = Bytes.toString(aneuploidy);
    Aneuploidy ap = new Aneuploidy(value.substring(0, value.indexOf("(")));

    int count = Integer.valueOf(value.substring(value.indexOf("(") + 1, value.length() - 1));
    if (gain)
      ap.gain(count);
    else
      ap.lose(count);

    aps.add(ap);
    }


  }
