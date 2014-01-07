/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import com.m6d.filecrush.crush.Crush;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.*;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
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

import static org.lcsb.lu.igcsa.mapreduce.FASTAUtil.*;

public class GenerateDerivativeChromosomes extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(GenerateDerivativeChromosomes.class.getName());

  private Configuration conf;
  private Scan scan;
  private Path output;
  private List<Location> filterLocations;

  public GenerateDerivativeChromosomes(Configuration conf, Scan scan, Path output, List<Location> filterLocations)
    {
    super(conf);
    this.conf = conf;
    this.scan = scan;
    this.output = output;
    this.filterLocations = filterLocations;
    }

  @Override
  public int run(String[] strings) throws Exception
    {
    Job job = new Job(conf, "Generate derivative FASTA files");
    job.setJarByClass(GenerateDerivativeChromosomes.class);

    job.setMapperClass(SequenceRequestMapper.class);
    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().getTableName(), scan, SequenceRequestMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);
    job.setReducerClass(SequenceFragmentReducer.class);
    job.setNumReduceTasks(filterLocations.size()); // one reducer for each segment
    job.setOutputFormatClass(NullOutputFormat.class);
    FileOutputFormat.setOutputPath(job, output);

    for (int order = 0; order < filterLocations.size(); order++)
      MultipleOutputs.addNamedOutput(job, Integer.toString(order), FASTAOutputFormat.class, LongWritable.class, Text.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  public static void main(String[] args) throws Exception
    {
    /*
    What this script actually should do is grab <all> karyotypes for a given parent and spin off jobs to generate each.
     */

    //args = new String[]{"kiss135"};
    args = new String[]{"kiss35"};

    if (args.length < 1)
      {
      System.err.println("Usage: GenerateFASTA <karyotype name>");
      System.exit(-1);
      }

    String karyotypeName = args[0];

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    HBaseKaryotype karyotype = admin.getKaryotype(karyotypeName);
    HBaseGenome genome = admin.getGenome(karyotype.getKaryotype().getParentGenome());

    for (AberrationResult aberration : karyotype.getAberrations())
      {
      if (aberration.getAbrType().equals("iso"))
        {
        log.warn("ISO ABERRATION, SKIPPING. We don't know yet how to deal with these");
        continue;
        }

      String fastaName = "der" + aberration.getAberrationDefinitions().get(0).getChromosome();

      log.info("Writing new derivative: " + fastaName);
      log.info(aberration.getAbrType() + " " + aberration.getAberrationDefinitions());

      // this FilterList will contain nested filter lists that putt all of the necessary locations
      AberrationLocationFilter alf = new AberrationLocationFilter();
      FilterList filterList = alf.getFilter(aberration, genome);

      // add to config so that the mapper/reducer have access to the order in which segments should be written

      SequenceRequestMapper.setLocations(alf.getFilterLocationList());
//      List<String> locations = new ArrayList<String>();
//      for (Location loc : alf.getFilterLocationList())
//        locations.add(loc.toString());
      //config.setStrings(SequenceRequestMapper.CFG_LOC, locations.toArray(new String[locations.size()]));

      if (aberration.getAbrType().equals("inv"))
        SequenceRequestMapper.setLocationsToReverse(alf.getFilterLocationList().get(1));
        //config.setInt(SequenceRequestMapper.REVERSE, 1); // it will always be the middle of the three locations

      // to create an appropriate FASTA header
      List<String> abrs = new ArrayList<String>();
      for (Location loc : aberration.getAberrationDefinitions())
        abrs.add(loc.getChromosome() + ":" + loc.getStart() + "-" + loc.getEnd());
      String abrDefinitions = aberration.getAbrType() + ":" + StringUtils.join(abrs.iterator(), ",");

      Scan scan = new Scan();
      scan.setFilter(filterList);

      Path output = new Path("/tmp/" + aberration.getGenome() + "/" + fastaName);
      FASTAOutputFormat.setLineLength(80);
      FASTAOutputFormat.addHeader(new Path(output, "0"), new FASTAHeader(fastaName, aberration.getGenome(), "parent=" + genome.getGenome().getName(), abrDefinitions));

      // Generate the segments for the new FASTA file
      GenerateDerivativeChromosomes gdc = new GenerateDerivativeChromosomes(config, scan, output, alf.getFilterLocationList());
      ToolRunner.run(gdc, null);
      gdc.fixOutputFiles(aberration);

      // created merged FASTA
      ToolRunner.run(new Crush(), new String[]{"--input-format=text", "--output-format=text", "--compress=none", output.toString(), output.toString() + ".fa"});
      log.info(output.toString());

      org.apache.commons.io.FileUtils.deleteDirectory(new File(output.toString()));
      }

    }

  // just to clean up the main method a bit
  protected void fixOutputFiles(AberrationResult aberration) throws IOException
    {
    // CRC files mess up any attempt to directly read/write from an unchanged file which means copying/moving fails too. Easiest fix right now is to dump the file.
    log.info("Deleting CRC files");
    deleteChecksumFiles(output.getFileSystem(conf), output);

    /*
  We now have output files.  In most cases the middle file(s) will be the aberration sequences.
  In many cases they can just be concatenated as is. Exceptions:
    - duplication: the middle file needs to be duplicated before concatenation
    - iso: there should be only 1 file, it needs to be duplicated in reverse before concatenation
    */
    if (aberration.getAbrType().equals("dup"))
      {
      log.info("DUP aberration");
      if (this.filterLocations.size() > 3)
        throw new IOException("This should not happen: dup has more than 3 locations");

      // move subsequent files
      for (int i = this.filterLocations.size() - 1; i > 1; i--)
        moveFile(output, Integer.toString(i), Integer.toString(i + 1));

      //then copy the duplicated segment
      copyFile(output.getFileSystem(conf), new Path(output, Integer.toString(1)), new Path(output, Integer.toString(2)));
      }
    // TODO Iso is different from inv in that I take the same segment and copy it in reverse.  But right now all I do is write out one segment.  Not sure quite how to handle that.
    if (aberration.getAbrType().equals("iso"))
      {
      log.info("ISO aberration");
      if (this.filterLocations.size() > 2)
        throw new IOException("This should not happen: iso has more than 2 locations");
      }

    }

  }
