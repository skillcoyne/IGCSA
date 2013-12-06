/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.generator.Aneuploidy;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.*;
import org.lcsb.lu.igcsa.hbase.fasta.AberrationWriter;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;
import org.lcsb.lu.igcsa.mapreduce.SequenceRequestMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    Configuration config = HBaseConfiguration.create();

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    List<AberrationResult> abrList = admin.getKaryotypeTable().queryTable(new Column("abr", "chr1", "1"));
    karyotypeName = abrList.get(1).getGenome();


    HBaseKaryotype karyotype = admin.getKaryotype(karyotypeName);
    //    String genomeName = karyotype.getKaryotype().getParentGenome();
    HBaseGenome genome = admin.getGenome("GRCh37");

    //    for (Aneuploidy pdy: karyotype.getKaryotype().getAneuploidy())
    //      log.info(pdy.toString());
    SingleColumnValueFilter genomeFilter = new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes("GRCh37"));

    for (AberrationResult aberration : karyotype.getAberrations())
      {
      String fastaName = "der" + aberration.getAberrationDefinitions().get(0).getChromosome();
      FASTAHeader header = new FASTAHeader("hb", fastaName, karyotypeName, "creating fasta from hbase");
      log.info("Writing new derivative: " + fastaName);
      //      FASTAWriter writer = new FASTAWriter(new File("/tmp/" + fastaName + ".fa"), new FASTAHeader("hb", fastaName, karyotypeName, "creating fasta from hbase"));

      log.info(aberration.getAbrType() + " " + aberration.getAberrationDefinitions());

      config.set(SequenceRequestMapper.CFG_ABR, aberration.getAbrType());
      List<String> locations = new ArrayList<String>();
      for (Location loc : aberration.getAberrationDefinitions())
        locations.add(loc.toString());


      Scan scan = new Scan();
      FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);

      config.setStrings(SequenceRequestMapper.CFG_LOC, locations.toArray(new String[locations.size()]));

      for (Location loc : aberration.getAberrationDefinitions())
        {
        log.info(loc.toString());

        long start = (loc.getStart() + 1000) / 1000;
        long stop = (loc.getEnd() + 1000) / 1000;

//        for (int i=(int)start; i<= stop; i++)
//          {
//          RowFilter filter = new RowFilter( CompareFilter.CompareOp.EQUAL,
//              new BinaryComparator(Bytes.toBytes(SequenceRow.createRowId("GRCh37", loc.getChromosome(), i))));
//          filterList.addFilter(filter);
//
//          log.info(filterList.getFilters().size());
//          }

        scan.setFilter(filterList);

                scan = new Scan();
        scan.setStartRow(Bytes.toBytes(SequenceRow.createRowId("GRCh37", loc.getChromosome(), start)));
        scan.setStopRow(Bytes.toBytes(SequenceRow.createRowId("GRCh37", loc.getChromosome(), stop)));

        Job job = Job.getInstance(config);
        job.setJarByClass(MutateFragments.class);

        job.setMapperClass(SequenceRequestMapper.class);
        TableMapReduceUtil.initTableMapperJob(admin.getSequenceTable().getTableName(), scan, SequenceRequestMapper.class, null, null, job);
        // because we aren't emitting anything from mapper
        job.setOutputFormatClass(NullOutputFormat.class);

        job.waitForCompletion(true);
        }


      // No, this is obviously not the best way to do things, I'm just getting tired of rewriting code to make it nice...
      //      if (aberration.getAbrType().equals("trans"))
      //        AberrationWriter.writeTranslocation(aberration, genome, writer);
      //      else if (aberration.getAberrationDefinitions().equals("del"))
      //        AberrationWriter.writeDeletion(aberration, genome, writer);
      //      else if (aberration.getAberrationDefinitions().equals("inv"))
      //        AberrationWriter.writeInversion(aberration, genome, writer);
      //      else if (aberration.getAberrationDefinitions().equals("dup"))
      //        AberrationWriter.writeDuplication(aberration, genome, writer);


      //      writer.flush();
      //      writer.close();
      break;
      }

    }


  }
