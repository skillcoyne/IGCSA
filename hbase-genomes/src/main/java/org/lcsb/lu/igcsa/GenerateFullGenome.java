/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;
import org.lcsb.lu.igcsa.mapreduce.*;

import java.io.IOException;
import java.util.*;


public class GenerateFullGenome extends Configured implements Tool
  {
  private static final Log log = LogFactory.getLog(GenerateFullGenome.class);

  private Configuration conf;
  private Scan scan;
  private Path output;
  private HBaseGenome genome;
  private FileSystem jobFS;

  public GenerateFullGenome(Configuration conf, Scan scan, Path output, HBaseGenome genome)
    {
    this.conf = conf;
    this.scan = scan;
    this.output = output;
    this.genome = genome;
    }

  public static void main(String[] args) throws Exception
    {
    if (args.length < 1)
      {
      System.err.println("Usage: GenerateFullGenome <genome name>");
      System.exit(-1);
      }
    String genomeName = args[0];

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);
    HBaseGenome genome = admin.getGenome(genomeName);

    List<String> chrs = new ArrayList<String>();
    for (HBaseChromosome chr : admin.getGenome(genomeName).getChromosomes())
      chrs.add(chr.getChromosome().getChrName());

    Scan scan = admin.getSequenceTable().getScanFor(new Column("info", "genome", genomeName));
    scan.setCaching(200);

    GenerateFullGenome gfg = new GenerateFullGenome(config, scan, new Path("/tmp/" + genomeName), genome);
    ToolRunner.run(gfg, chrs.toArray(new String[chrs.size()]));

    // remove extraneous files and rename to .fa
    gfg.cleanUpFiles(chrs);
    }

  protected void cleanUpFiles(List<String> chrs) throws IOException
    {
    FASTAUtil.deleteChecksumFiles(jobFS, output);
    for (String c: chrs)
      FileUtil.copy(jobFS, new Path(output, c), jobFS, new Path(output, c+".fa"), true, false, jobFS.getConf());
    }


  @Override
  public int run(String[] chrs) throws Exception
    {
    Job job = new Job(conf, "Generate normal FASTA files");
    job.setJarByClass(GenerateDerivativeChromosomes.class);

    job.setMapperClass(ChromosomeSequenceMapper.class);
    ChromosomeSequenceMapper.setChromosomes(job, chrs);
    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().getTableName(), scan, ChromosomeSequenceMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    job.setReducerClass(ChromosomeSequenceReducer.class);
    job.setNumReduceTasks(chrs.length); // one reducer for each segment
    job.setOutputFormatClass(NullOutputFormat.class);

    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 80);
    for (String chr : chrs)
      {
      MultipleOutputs.addNamedOutput(job, chr, FASTAOutputFormat.class, LongWritable.class, Text.class);
      FASTAOutputFormat.addHeader(job, new Path(output, chr), new FASTAHeader("chr" + chr, genome.getGenome().getName(),
          "parent=" + genome.getGenome().getParent(), "hbase-generation"));
      }

    this.jobFS = output.getFileSystem(conf);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  static class ChromosomeSequenceMapper extends TableMapper<SegmentOrderComparator, FragmentWritable>
    {
    private static final Log log = LogFactory.getLog(ChromosomeSequenceMapper.class);

    private List<String> chrs;

    protected static void setChromosomes(Job job, String... chrs)
      {
      job.getConfiguration().setStrings("chromosomes", chrs);
      }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      chrs = new ArrayList<String>(context.getConfiguration().getStringCollection("chromosomes"));
      }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
      {
      SequenceResult sr = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);
      String sequence = sr.getSequence();
      SegmentOrderComparator soc = new SegmentOrderComparator(chrs.indexOf(sr.getChr()), sr.getSegmentNum());

      FragmentWritable fw = new FragmentWritable(sr.getChr(), sr.getStart(), sr.getEnd(), sr.getSegmentNum(), sequence);

      log.debug(sr.getChr() + " " + sr.getSegmentNum());

      context.write(soc, fw);
      }
    }

  static class ChromosomeSequenceReducer extends Reducer<SegmentOrderComparator, FragmentWritable, LongWritable, Text>
    {
    private static final Log log = LogFactory.getLog(ChromosomeSequenceReducer.class);

    private MultipleOutputs mos;
    private List<String> chrs;

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException
      {
      mos.close();
      }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      mos = new MultipleOutputs(context);
      chrs = new ArrayList<String>(context.getConfiguration().getStringCollection("chromosomes"));
      }

    @Override
    protected void reduce(SegmentOrderComparator key, Iterable<FragmentWritable> values, Context context) throws IOException, InterruptedException
      {
      log.debug("Order " + key.getOrder() + ":" + key.getSegment());

      Iterator<FragmentWritable> fI = values.iterator();
      while (fI.hasNext())
        {
        FragmentWritable fw = fI.next();
        LongWritable segmentKey = new LongWritable(fw.getSegment());

        String namedOutput = chrs.get((int) key.getOrder());
        mos.write(namedOutput, segmentKey, new Text(fw.getSequence()));
        }
      }

    }


  }
