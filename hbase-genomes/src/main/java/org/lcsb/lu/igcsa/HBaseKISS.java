/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.genome.Karyotype;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;

import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class HBaseKISS
  {
  static Logger log = Logger.getLogger(HBaseKISS.class.getName());

  // Spring
  private ApplicationContext context;

  private HBaseGenome reference;
  private String karyotypeName;

  public HBaseKISS(ApplicationContext context, CommandLine cl) throws Exception
    {
    this.context = context;

    karyotypeName = String.valueOf(new Random().nextInt(Math.abs((int) (System.currentTimeMillis()))));
    if (cl.hasOption('n'))
      karyotypeName = cl.getOptionValue('n');

    // default to reference genome
    String parentName = null;
    if (cl.hasOption('p'))
      parentName = cl.getOptionValue('p');

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin();
    reference = admin.getReference();

    if (parentName != null)
      reference = admin.getGenome(parentName);
    }

  public void createKaryotype(Karyotype karyotype)
    {
    for (Aberration abr: karyotype.getAberrationDefinitions())
      {
      String hbabr = createHBaseEntry(abr);

      }

    }


  private String createHBaseEntry(Aberration abr)
    {
    String dbe = abr.getAberration().toString();

    List<String> aberrations = new ArrayList<String>();
    for (Band b: abr.getBands())
      aberrations.add( b.getChromosomeName() + ":" + b.getLocation().getStart() + "-" + b.getLocation().getEnd() );

    dbe = dbe + "(" + StringUtils.join(aberrations.iterator(), ",") + ")";

    return dbe;
    }

  }
