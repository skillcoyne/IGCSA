/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.*;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.mapreduce.FASTAOutputFormat;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.mapreduce.SequenceFragmentReducer;
import org.lcsb.lu.igcsa.mapreduce.SequenceRequestMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateFASTA  extends Configured implements Tool
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
    HBaseGenome genome = admin.getGenome(karyotype.getKaryotype().getParentGenome());



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

      config.setStrings(SequenceRequestMapper.CFG_LOC, locations.toArray(new String[locations.size()]));

      // this FilterList will contain nested filter lists that putt all of the necessary locations
      FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);

      List<String> abrs = new ArrayList<String>();
      for (Location loc : aberration.getAberrationDefinitions())
        {
        log.info(loc.toString());

        long start = (loc.getStart() + 1000) / 1000;
        long stop = (loc.getEnd() + 1000) / 1000;

        RowFilter range1 = new RowFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(SequenceRow.createRowId("GRCh37", loc.getChromosome(), start))));
        RowFilter range2 = new RowFilter(CompareFilter.CompareOp.LESS, new BinaryComparator(Bytes.toBytes(SequenceRow.createRowId("GRCh37", loc.getChromosome(), stop))));

        FilterList range = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        range.addFilter(range1);
        range.addFilter(range2);

        filterList.addFilter(range);

        abrs.add( loc.getChromosome() + ":" + loc.getStart() + "-" + loc.getEnd() );
        }
      Scan scan = new Scan();
      scan.setFilter(filterList);

      Path output = new Path("/tmp/" + aberration.getGenome() + "/" + fastaName);

      // for now anyhow
      org.apache.commons.io.FileUtils.deleteDirectory(new File(output.toString()));

      String abrDefinitions = aberration.getAbrType() + ":" + StringUtils.join(abrs.iterator(), ",");
      config.set(FASTAOutputFormat.FASTA_LINE_LENGTH, "70");
      config.set(FASTAOutputFormat.FASTA_HEADER, new FASTAHeader(fastaName,
          aberration.getGenome(),
          "parent="+genome.getGenome().getName(),
          abrDefinitions).getFormattedHeader());

      Job job = new Job(config);
      job.setJarByClass(GenerateFASTA.class);

      job.setMapperClass(SequenceRequestMapper.class);
      TableMapReduceUtil.initTableMapperJob(admin.getSequenceTable().getTableName(), scan, SequenceRequestMapper.class, SequenceFragmentReducer.SegmentOrderComparator.class, FragmentWritable.class, job);

      job.setReducerClass(SequenceFragmentReducer.class);
      job.setOutputFormatClass(FASTAOutputFormat.class);

      FileOutputFormat.setOutputPath(job, output);

      job.setOutputKeyClass(LongWritable.class);
      job.setOutputValueClass(Text.class);

      job.waitForCompletion(true);


      /*
      At this point each derivative chromosome is in a part-r-0000NNN file
       */
      //FileUtils.moveDirectory( new File(output.getName()), new File("/tmp", fastaName + ".fa"));


      break;
      }

    }


  @Override
  public int run(String[] strings) throws Exception
    {

    return 0;
    }
  }
