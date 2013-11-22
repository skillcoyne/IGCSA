/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;

import java.util.ArrayList;
import java.util.List;

public class CreateKaryotypes
  {
  static Logger log = Logger.getLogger(CreateKaryotypes.class.getName());

  public static void main(String[] args) throws Exception
    {

    String parentGenome = "GRCh37";
    // TODO collect aneuploidies & aberrations, put into HBase tables.  Write extractor job that will assemble a karyotype from the HBase tables

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin();

    List<MinimalKaryotype> pop = new PopulationGenerator().run(5);

    admin.getGenome(parentGenome).createKaryotype("kiss", "GRCh37", pop);

    }


  }
