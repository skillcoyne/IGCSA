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


  /*
  Pipeline:

  1. LoadFromFASTA.  Only needs to run once per set of FASTA files so this is a standalone process.
  2. MutateFragments (possibly)
  3. CreateKaryotypes
  4. Generate chromosomes (GenerateFullGenome & GenerateDerivativeChromosomes)
  5. Run streaming job to align
  6. Assess alignment
  7. Repeat 3-6
   */

  public static void main(String[] args) throws Exception
    {
    // Required... parent genome name, bwa, read pair





    }





  }
   //    ProgramDriver pgd = new ProgramDriver();
   //    try
   //      {
   //      pgd.addClass(LoadFromFASTA.class.getSimpleName(), LoadFromFASTA.class, "Loads the HBase database from the provided FASTA files.");
   //      pgd.addClass(CreateKaryotypes.class.getSimpleName(), CreateKaryotypes.class, "Generates karyotypes for the given genome.");
   //      pgd.addClass(GenerateDerivativeChromosomes.class.getSimpleName(), GenerateDerivativeChromosomes.class, "Generate FASTA files for a karyotype.");
   //      pgd.addClass(GenerateFullGenome.class.getSimpleName(), GenerateFullGenome.class, "Generate FASTA files for a normal genome.");
   //      pgd.addClass(MutateFragments.class.getSimpleName(), MutateFragments.class, "Generate genome with small-scale mutations.");
   //      }
   //    catch (Throwable throwable)
   //      {
   //      throwable.printStackTrace();
   //      }
