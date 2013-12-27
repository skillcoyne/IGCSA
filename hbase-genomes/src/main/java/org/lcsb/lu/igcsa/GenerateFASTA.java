/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.*;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.*;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.mapreduce.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateFASTA  extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(GenerateFASTA.class.getName());


  public static void main(String[] args) throws Exception
    {
    args = new String[]{"kiss135"};

    if (args.length < 1)
      {
      System.err.println("Usage: GenerateFASTA <karyotype name>");
      System.exit(-1);
      }

    String karyotypeName = args[0];

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

//    List<AberrationResult> abrList = admin.getKaryotypeTable().queryTable(new Column("abr", "chr1", "1"));
//    karyotypeName = abrList.get(1).getGenome();

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

      // this FilterList will contain nested filter lists that putt all of the necessary locations
      AberrationLocationFilter alf = new AberrationLocationFilter();
//      AberrationLocationFilter alf = AberrationFilter.getByCytogenetic(aberration.getAbrType()).getInstance();
      FilterList filterList = alf.getFilter(aberration, genome);

      // add to config so that the mapper/reducer have access to the order in which segments should be written
      List<String> locations = new ArrayList<String>();
      for (Location loc : alf.getFilterLocationList())
        locations.add(loc.toString());
      config.setStrings(SequenceRequestMapper.CFG_LOC, locations.toArray(new String[locations.size()]));

      // to create an appropriate FASTA header
      List<String> abrs = new ArrayList<String>();
      for (Location loc : aberration.getAberrationDefinitions())
        abrs.add( loc.getChromosome() + ":" + loc.getStart() + "-" + loc.getEnd() );
      String abrDefinitions = aberration.getAbrType() + ":" + StringUtils.join(abrs.iterator(), ",");


      Scan scan = new Scan();
      scan.setFilter(filterList);

      Path output = new Path("/tmp/" + aberration.getGenome() + "/" + fastaName);
      // for now anyhow
      org.apache.commons.io.FileUtils.deleteDirectory(new File(output.toString()));

      config.set(FASTAOutputFormat.FASTA_LINE_LENGTH, "70");
      config.set(FASTAOutputFormat.FASTA_HEADER, new FASTAHeader(fastaName,
          aberration.getGenome(),
          "parent="+genome.getGenome().getName(),
          abrDefinitions).getFormattedHeader());

      Job job = new Job(config, "Generate derivative FASTA files");
      job.setJarByClass(GenerateFASTA.class);

      job.setMapperClass(SequenceRequestMapper.class);
      TableMapReduceUtil.initTableMapperJob(admin.getSequenceTable().getTableName(), scan, SequenceRequestMapper.class, SequenceFragmentReducer.SegmentOrderComparator.class, FragmentWritable.class, job);

      job.setReducerClass(SequenceFragmentReducer.class);
      //job.setOutputFormatClass(FASTAOutputFormat.class);
      //job.setOutputFormatClass(MultipleFASTASegmentOutputFormat.class);

      for(Location loc: alf.getFilterLocationList())
        MultipleOutputs.addNamedOutput(job, (loc.getChromosome() +  loc.getStart() +  loc.getEnd()), FASTAOutputFormat.class, LongWritable.class, Text.class);

      FileOutputFormat.setOutputPath(job, output);

//      job.setOutputKeyClass(LongWritable.class);
//      job.setOutputValueClass(Text.class);

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
