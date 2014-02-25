/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;

import java.util.List;

public class CreateKaryotypes
  {
  static Logger log = Logger.getLogger(CreateKaryotypes.class.getName());

  public static void main(String[] args) throws Exception
    {
    if (args.length < 2)
      {
      System.err.println("Usage: CreateKaryotypes <parent genome name> <karyotype name>");
      System.exit(-1);
      }
    String parentGenome = args[0];

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin();
    if (admin.getGenomeTable().getGenome(parentGenome) == null && !parentGenome.equalsIgnoreCase("test"))
      throw new Exception("Genome " + parentGenome + " does not exist. Exiting.");
    admin.deleteKaryotypes(parentGenome);

    String karyotypePrefix = StringUtils.join(args, '-');
    List<MinimalKaryotype> pop = new PopulationGenerator().run(1000);

    int i = 1;
    for (MinimalKaryotype mk : pop)
      {
      log.info(mk);
      String karyotypeRowId = admin.getKaryotypeIndexTable().addKaryotype(karyotypePrefix + i, parentGenome, mk);
      admin.getKaryotypeTable().addAberrations(karyotypeRowId, mk.getAberrations());
      ++i;
      }
    }


  }
