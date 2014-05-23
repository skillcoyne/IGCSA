/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ProgramDriver;
import org.lcsb.lu.igcsa.generators.GenerateChromosomes;
import org.lcsb.lu.igcsa.generators.GenerateFullGenome;
import org.lcsb.lu.igcsa.generators.GenerateFullKaryotype;


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
      pgd.addClass("fastaload", LoadFromFASTA.class, "Loads a genome into the HBase database from the provided FASTA files. Args: -g [genome name] -f [hdfs path to FASTA file directory] ");

      pgd.addClass("mutate", MutateFragments.class, "Generate genome with small-scale mutations. Args: -p [reference genome, ex. GRCh37] -m [new genome name]");
      pgd.addClass("hbaseutil", org.lcsb.lu.igcsa.hbase.HBaseUtility.class, "Import/Export HBase tables from/to hdfs or s3. Args: -d [hdfs directory for read/write] -c [IMPORT|EXPORT] -t [comma separated list of tables OPTIONAL]");

      pgd.addClass("karygen", GenerateFullKaryotype.class, "Generates karyotypes for the given genome.");
      pgd.addClass("genchr", GenerateChromosomes.class, "Generates derivative chromosomes based on the provided bands. ");
      pgd.addClass("gennormal", GenerateFullGenome.class, "Generate FASTA files for a normal genome. Args: -g [genome name, ex. GRCh37] -o [hdfs output path for FASTA files]");

      pgd.driver(args);
      }
    catch (Throwable throwable)
      {
      throwable.printStackTrace();
      }

    }





  }
