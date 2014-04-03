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
    ProgramDriver pgd = new ProgramDriver();
    try
      {
      pgd.addClass("fastaload", LoadFromFASTA.class, "Loads the HBase database from the provided FASTA files.");
      pgd.addClass("karygen", CreateKaryotypes.class, "Generates karyotypes for the given genome.");
      pgd.addClass("karyofasta", GenerateDerivativeChromosomes.class, "Generate FASTA files for a karyotype.");
      pgd.addClass("gennormal", GenerateFullGenome.class, "Generate FASTA files for a normal genome.");
      pgd.addClass("mutate", MutateFragments.class, "Generate genome with small-scale mutations.");
      pgd.addClass("hbaseutil", org.lcsb.lu.igcsa.hbase.HBaseUtility.class, "Import/Export HBase tables from/to hdfs or s3.");

      pgd.driver(args);
      }
    catch (Throwable throwable)
      {
      throwable.printStackTrace();
      }




    }





  }
