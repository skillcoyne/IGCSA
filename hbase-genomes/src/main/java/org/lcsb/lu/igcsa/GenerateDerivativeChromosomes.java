/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
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
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.*;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.mapreduce.*;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil;

import java.util.ArrayList;
import java.util.List;

import static org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil.*;

public class GenerateDerivativeChromosomes extends JobIGCSA
  {
  static Logger log = Logger.getLogger(GenerateDerivativeChromosomes.class.getName());

  private Scan scan;
  private Path output;
  private List<Location> filterLocations;
  private String aberrationType;
  private FASTAHeader header;

  public GenerateDerivativeChromosomes(Configuration conf, Scan scan, Path output, List<Location> filterLocations, FASTAHeader header, String abrType)
    {
    super(conf);
    this.scan = scan;
    this.output = output;
    this.filterLocations = filterLocations;
    this.aberrationType = abrType;
    this.header = header;
    }

  @Override
  public int run(String[] strings) throws Exception
    {
    Job job = new Job(getConf(), "Generate derivative FASTA files");
    job.setJarByClass(GenerateDerivativeChromosomes.class);

    // M/R setup
    job.setMapperClass(SequenceRequestMapper.class);
    SequenceRequestMapper.setLocations(job, filterLocations);
    if (aberrationType.equals("inv"))
      SequenceRequestMapper.setLocationsToReverse(job, filterLocations.get(1));

    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().getTableName(), scan, SequenceRequestMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    // custom partitioner to make sure the segments go to the correct reducer sorted
    job.setPartitionerClass(FragmentPartitioner.class);

    job.setReducerClass(SequenceFragmentReducer.class);
    job.setNumReduceTasks(filterLocations.size()); // one reducer for each segment

    // Output format setup
    job.setOutputFormatClass(NullOutputFormat.class);
    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 70);
    FASTAOutputFormat.addHeader(job, new Path(output, "0"), header);
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
    //args = new String[]{"kiss35"};
    if (args.length < 1)
      {
      System.err.println("Usage: GenerateFASTA <karyotype name>");
      System.exit(-1);
      }

    String karyotypeName = args[0];

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    HBaseKaryotype karyotype = admin.getKaryotype(karyotypeName);
    HBaseGenome parentGenome = admin.getGenome(karyotype.getKaryotype().getParentGenome());

    Path basePath = new Path("/tmp"); // TODO this should probably be an arg
    Path karyotypePath = new Path(basePath, karyotype.getKaryotype().getParentGenome());
    if (karyotypePath.getFileSystem(config).exists(karyotypePath))
      karyotypePath.getFileSystem(config).delete(karyotypePath, true);

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
      FilterList filterList = alf.getFilter(aberration, parentGenome);

      log.info(filterList);

      // to create an appropriate FASTA header
      List<String> abrs = new ArrayList<String>();
      for (Location loc : aberration.getAberrationDefinitions())
        abrs.add(loc.getChromosome() + ":" + loc.getStart() + "-" + loc.getEnd());
      String abrDefinitions = aberration.getAbrType() + ":" + StringUtils.join(abrs.iterator(), ",");

      Scan scan = new Scan();
      scan.setFilter(filterList);

      Path baseOutput = new Path("/tmp/" + aberration.getGenome() + "/" + fastaName);

      // Generate the segments for the new FASTA file
      GenerateDerivativeChromosomes gdc = new GenerateDerivativeChromosomes(config, scan, baseOutput, alf.getFilterLocationList(), new FASTAHeader(fastaName, aberration.getGenome(), "parent=" + parentGenome.getGenome().getName(), abrDefinitions), aberration.getAbrType());

      ToolRunner.run(gdc, null);
      gdc.fixOutputFiles(aberration);
      }

    //Create BWA index with ONLY the derivative chromosomes
    final Path mergedFASTA = new Path(new Path(basePath, karyotypeName), "reference.fa");
    // Create a single merged FASTA file for use in the indexing step
    FASTAUtil.mergeFASTAFiles(basePath.getFileSystem(config), new Path(basePath, karyotypeName).toString(), mergedFASTA.toString());

    // Run BWA
    Path tmp = BWAIndex.writeReferencePointerFile(mergedFASTA, FileSystem.get(config));
    ToolRunner.run(new BWAIndex(), new String[]{tmp.toString()});
    FileSystem.get(config).delete(tmp, false);
    }

  // just to clean up the main method a bit
  protected void fixOutputFiles(AberrationResult aberration) throws Exception
    {
    // CRC files mess up any attempt to directly read/write from an unchanged file which means copying/moving fails too. Easiest fix right now is to dump the file.
    deleteChecksumFiles(this.getJobFileSystem(), output);
    /*
  We now have output files.  In most cases the middle file(s) will be the aberration sequences.
  In many cases they can just be concatenated as is. Exceptions:
    - duplication: the middle file needs to be duplicated before concatenation
    - iso: there should be only 1 file, it needs to be duplicated in reverse before concatenation
    */
    FileSystem jobFS = this.getJobFileSystem();

    log.info(jobFS.getWorkingDirectory());
    if (aberration.getAbrType().equals("dup"))
      {
      log.info("DUP aberration");
      if (this.filterLocations.size() > 3)
        throw new RuntimeException("This should not happen: dup has more than 3 locations");

      // move subsequent files
      for (int i = this.filterLocations.size() - 1; i > 1; i--)
        {
        // move files
        FileUtil.copy(jobFS, new Path(output, Integer.toString(i)), jobFS, new Path(output, Integer.toString(i + 1)), true, false, jobFS.getConf());
        }
      //then copy the duplicated segment
      FileUtil.copy(jobFS, new Path(output, Integer.toString(1)), jobFS, new Path(output, Integer.toString(2)), false, false, jobFS.getConf());
      }
    // TODO Iso is different from inv in that I take the same segment and copy it in reverse.  But right now all I do is write out one segment.  Not sure quite how to handle that.
    if (aberration.getAbrType().equals("iso"))
      {
      log.info("ISO aberration");
      if (this.filterLocations.size() > 2)
        throw new RuntimeException("This should not happen: iso has more than 2 locations");
      }

    // create merged FASTA at chromosome level -- there is an issue here that it just concatenates the files which means at the merge points there are strings of different lengths.  This is an issue in samtools.
    FASTAUtil.mergeFASTAFiles(jobFS, output.toString(), output.toString() + ".fa");
    jobFS.delete(output, true);
    }

  }
