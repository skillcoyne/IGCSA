package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hbase.HBaseConfiguration;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAFragmentInputFormat;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAFragmentMapper;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.population.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collection;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LoadFromFASTA extends JobIGCSA
  {
  static Logger log = Logger.getLogger(LoadFromFASTA.class.getName());

  public LoadFromFASTA()
    {
    super(HBaseConfiguration.create());

    Option genome = new Option("g", "genome", true, "Genome name.");
    genome.setRequired(true);
    this.addOptions(genome);

    Option fasta = new Option("f", "FASTA path", true, "Fully qualified path in HDFS or S3 to read FASTA files.");
    fasta.setRequired(true);
    this.addOptions(fasta);
    }

  @Override
  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = new GenericOptionsParser(new Configuration(), args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs(), this.getClass());
    if (args.length < 2)
      {
      System.err.println("Usage: " + LoadFromFASTA.class.getSimpleName() + " -g <genome name> -f <directory with fasta files>");
      System.exit(-1);
      }

    Configuration conf = gop.getConfiguration();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);
    admin.createTables();

    String genomeName = cl.getOptionValue("g");
    getConf().set("genome", genomeName);

    String fastaDir = cl.getOptionValue("f");

    if (admin.getGenomeTable().getGenome(genomeName) != null)
      {
      System.out.println("Genome '" + genomeName + "' already exists, overwrites are not allowed. Exiting.");
      System.exit(-1);
      }

    // create genome if it doesn't exist
    if (admin.getGenomeTable().getGenome(genomeName) == null)
      admin.getGenomeTable().addGenome(genomeName, null);
    admin.closeConections();

    Collection<Path> filePaths = new ArrayList<Path>();
    FileSystem fs = FileSystem.get(conf);

    if (fastaDir.startsWith("s3"))
      fs = FileSystem.get(new Path(fastaDir).toUri(), conf);

    FileStatus[] statuses = fs.listStatus(new Path(fastaDir), new PathFilter()
    {
    @Override
    public boolean accept(Path path)
      {
      return FileUtils.FASTA_FILE.accept(null, path.getName());
      }
    });
    for (FileStatus status : statuses)
      filePaths.add(status.getPath());

    Job job = new Job(getConf(), "Genome Fragmentation: " + genomeName);

    job.setSpeculativeExecution(false);
    job.setReduceSpeculativeExecution(false);

    job.setJarByClass(LoadFromFASTA.class);
    job.setMapperClass(FASTAFragmentMapper.class);

    job.setMapOutputKeyClass(LongWritable.class);
    job.setMapOutputValueClass(FragmentWritable.class);

    job.setInputFormatClass(FASTAFragmentInputFormat.class);

    for (Path path : filePaths)
      {
      //long len = path.getFileSystem(getConf()).getContentSummary(path).getLength();
      log.info(path.toString());
      FileInputFormat.addInputPath(job, path);
      }

    // because we aren't emitting anything from mapper
    job.setNumReduceTasks(0);
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new LoadFromFASTA(), args);
    }

  }




