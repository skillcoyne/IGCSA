/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ProgramDriver;


public class MainApp
  {
  private static final Log log = LogFactory.getLog(MainApp.class);

  public static void main(String[] args) throws Exception
    {
    if (args.length < 1)
      {
      System.err.println("One of the following is required: LoadFromFASTA, MutateFragments, CreateKaryotypes, GenerateFASTA");
      System.exit(-1);
      }

    String appName = args[0];
    String[] remainingArgs = (String[]) ArrayUtils.subarray(args, 1, args.length);

    ProgramDriver pgd = new ProgramDriver();
    try
      {
      pgd.addClass(LoadFromFASTA.class.getSimpleName(), LoadFromFASTA.class, "Loads the HBase database from the provided FASTA files.");
      pgd.addClass(CreateKaryotypes.class.getSimpleName(), CreateKaryotypes.class, "Generates karyotypes for the given genome.");
      pgd.addClass(GenerateDerivativeChromosomes.class.getSimpleName(), GenerateDerivativeChromosomes.class, "Generate FASTA files for a karyotype.");
      pgd.addClass(GenerateFullGenome.class.getSimpleName(), GenerateFullGenome.class, "Generate FASTA files for a normal genome.");
      pgd.addClass(MutateFragments.class.getSimpleName(), MutateFragments.class, "Generate genome with small-scale mutations.");
      }
    catch (Throwable throwable)
      {
      throwable.printStackTrace();
      }


    }


  }
