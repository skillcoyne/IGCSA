/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.job;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.population.MinimalKaryotype;
import org.lcsb.lu.igcsa.population.PopulationGenerator;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

//    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin();
//    if (admin.getGenomeTable().getGenome(parentGenome) == null && !parentGenome.equalsIgnoreCase("test"))
//      throw new Exception("Genome " + parentGenome + " does not exist. Exiting.");
//    admin.deleteKaryotypes(parentGenome);

    String karyotypePrefix = StringUtils.join(args, '-');
    List<MinimalKaryotype> pop = new PopulationGenerator().run(1000, 75);

    int i = 1;
    Set<Aberration> aberrationSet = new HashSet<Aberration>();
    for (MinimalKaryotype mk : pop)
      {
      //log.info(mk);
      for (Aberration abr: mk.getAberrations())
        {
        if (abr.getAberration().equals(AberrationTypes.TRANSLOCATION))
          {
          //log.info(abr.getBands());
          aberrationSet.add(abr);
          }
        }
//      String karyotypeRowId = admin.getKaryotypeIndexTable().addKaryotype(karyotypePrefix + i, parentGenome, mk);
//      admin.getKaryotypeTable().addAberrations(karyotypeRowId, mk.getAberrations());
      ++i;
      }
    //log.info(bandSet.toString());
    Aberration abr = aberrationSet.iterator().next();
    String bands = abr.getBands().get(0).getFullName() + "," + abr.getBands().get(1).getFullName();
    log.info(bands);
    log.info("Total band pairs " + aberrationSet.size());
    }


  }
