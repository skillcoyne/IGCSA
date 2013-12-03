/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.Aneuploidy;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.*;
import org.lcsb.lu.igcsa.hbase.fasta.AberrationWriter;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class GenerateFASTA
  {
  static Logger log = Logger.getLogger(GenerateFASTA.class.getName());


  public static void main(String[] args) throws Exception
    {
    args = new String[]{"kiss188"};

    if (args.length < 1)
      {
      System.err.println("Usage: GenerateFASTA <karyotype name>");
      System.exit(-1);
      }

    String karyotypeName = args[0];

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin();

    HBaseKaryotype karyotype = admin.getKaryotype(karyotypeName);
    String genomeName = karyotype.getKaryotype().getParentGenome();
    HBaseGenome genome = admin.getGenome(genomeName);

    //    for (Aneuploidy pdy: karyotype.getKaryotype().getAneuploidy())
    //      log.info(pdy.toString());


    for (AberrationResult aberration : karyotype.getAberrations())
      {
      String fastaName = "der" + aberration.getAberrationDefinitions().get(0).getChromosome();
      log.info("Writing new derivative: " + fastaName);
      FASTAWriter writer = new FASTAWriter(new File("/tmp/" + fastaName + ".fa"), new FASTAHeader("hb", fastaName, karyotypeName, "creating fasta from hbase"));

      log.info(aberration.getAbrType() + " " + aberration.getAberrationDefinitions());

      // No, this is obviously not the best way to do things, I'm just getting tired of rewriting code to make it nice...
      if (aberration.getAbrType().equals("trans"))
        AberrationWriter.writeTranslocation(aberration, genome, writer);
      else if (aberration.getAberrationDefinitions().equals("del"))
        AberrationWriter.writeDeletion(aberration, genome, writer);
      else if (aberration.getAberrationDefinitions().equals("inv"))
        AberrationWriter.writeInversion(aberration, genome, writer);
      else if (aberration.getAberrationDefinitions().equals("dup"))
        AberrationWriter.writeDuplication(aberration, genome, writer);


      writer.flush();
      writer.close();
      }

    }


  }
