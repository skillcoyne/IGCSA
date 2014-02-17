package org.lcsb.lu.igcsa;

import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Pipeline
  {
  static Logger log = Logger.getLogger(Pipeline.class.getName());

  public static void main(String[] args) throws Exception
    {

    }

  private static void parseCommandLine(String[] args)
    {
    Options options = new Options();
    options.addOption("r", "read-pair", true, "Read pair TSV file");
    //options.addOption()
    }


  /*
  Inputs: read pair fastq files, parent genome
  Process:
    1. align against parent
    2. generate karyotypes
    3. create -all- derivatives
    4. align against -all- karyotype genomes
    5. evaluate...??
   */

  public void Pipeline()
    {



    }

  }
