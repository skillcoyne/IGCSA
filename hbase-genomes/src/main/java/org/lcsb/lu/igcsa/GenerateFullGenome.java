/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
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
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;
import org.lcsb.lu.igcsa.mapreduce.*;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAOutputFormat;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil;

import java.io.IOException;
import java.util.*;


public class GenerateFullGenome extends BWAJob
  {
  public static void main(String[] args) throws Exception
    {
    GenerateFullGenome gfg = new GenerateFullGenome();
    ToolRunner.run(gfg, args);

    // remove extraneous files and rename to .fa
    gfg.cleanUpFiles();
    // Create a single merged FASTA file for use in the indexing step
    Path mergedFasta = new Path(gfg.getOutputPath(), "reference.fa");
    FASTAUtil.mergeFASTAFiles(gfg.getJobFileSystem(), gfg.getOutputPath().toString(), mergedFasta.toString());

    // Run BWA index
    Path tmp = BWAIndex.writeReferencePointerFile(mergedFasta, gfg.getJobFileSystem());

    ToolRunner.run(new BWAIndex(), (String[]) ArrayUtils.addAll(args, new String[]{"-f", tmp.toString()}));

    gfg.getJobFileSystem().delete(tmp, true);
    }


  private static final Log log = LogFactory.getLog(GenerateFullGenome.class);

  private Path output;
  private HBaseGenome genome;
  private String genomeName;
  private List<String> chromosomes;

  public GenerateFullGenome()
    {
    super(new Configuration());

    Option genome = new Option("g", "genome", true, "Genome name.");
    genome.setRequired(true);
    this.addOptions( genome );
    }

  public List<String> getChromosomes()
    {
    return chromosomes;
    }

  public Path getOutputPath()
    {
    return output;
    }


  protected void cleanUpFiles() throws IOException
    {
    FASTAUtil.deleteChecksumFiles(getJobFileSystem(), output);
    for (String c : chromosomes)
      FileUtil.copy(getJobFileSystem(), new Path(output, c), getJobFileSystem(), new Path(output, c + ".fa"), true, false, getConf());
    }


  private Scan setup() throws IOException
    {
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    genome = admin.getGenome(genomeName);

    chromosomes = new ArrayList<String>();
    Scan scan = admin.getSequenceTable().getScanFor(new Column("info", "genome", genomeName));
    scan.setCaching(200);

    if (genomeName.equals("test"))
      {
      // should be 59129 rows
      chromosomes.add("19");
      scan = admin.getSequenceTable().getScanFor(new Column("info", "genome", "GRCh37"), new Column("loc", "chr", chromosomes.get(0)));
      genome = admin.getGenome("GRCh37");
      }
    else
      {
      for (HBaseChromosome chr : admin.getGenome(genomeName).getChromosomes())
        chromosomes.add(chr.getChromosome().getChrName());
      }

    output = new Path(getPath(Paths.GENOMES), genomeName);
    if (!getJobFileSystem().getUri().toASCIIString().startsWith("hdfs"))
      output = new Path("/tmp/" + getPath(Paths.GENOMES), genomeName);

    if (getJobFileSystem().exists(output))
      getJobFileSystem().delete(output, true);

    return scan;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());

    genomeName = cl.getOptionValue("g");
    Scan scan = setup();

    log.info(scan);

    Job job = new Job(getConf(), "Generate FASTA files for " + genomeName);
    job.setJarByClass(GenerateFullGenome.class);

    job.setMapperClass(ChromosomeSequenceMapper.class);
    ChromosomeSequenceMapper.setChromosomes(job, chromosomes.toArray(new String[chromosomes.size()]));

    TableMapReduceUtil.initTableMapperJob(HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().getTableName(), scan, ChromosomeSequenceMapper.class, SegmentOrderComparator.class, FragmentWritable.class, job);

    // partitioner is required to make sure all fragments from a given chromosome go to the same reducers
    job.setPartitionerClass(FragmentPartitioner.class);

    job.setReducerClass(ChromosomeSequenceReducer.class);
    job.setNumReduceTasks(chromosomes.size()); // one reducer for each segment
    job.setOutputFormatClass(NullOutputFormat.class);

    FileOutputFormat.setOutputPath(job, output);
    FASTAOutputFormat.setLineLength(job, 70);
    for (String chr : chromosomes)
      {
      //getConf().setInt();
      MultipleOutputs.addNamedOutput(job, chr, FASTAOutputFormat.class, LongWritable.class, Text.class);
      FASTAOutputFormat.addHeader(job, new Path(output, chr), new FASTAHeader("chr" + chr, genome.getGenome().getName(), "parent=" + genome.getGenome().getParent(), "hbase-generation"));
      }

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  /****************************
  MAPPER & REDUCER
   ****************************/
  static class ChromosomeSequenceMapper extends TableMapper<SegmentOrderComparator, FragmentWritable>
    {
    private static final Log log = LogFactory.getLog(ChromosomeSequenceMapper.class);

    private List<String> chrs;


    protected static void setChromosomes(Job job, String... chrs)
      {
      job.getConfiguration().setStrings("chromosomes", chrs);
      log.info("Setting chromosomes in config: " + chrs);
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
      if (soc.getOrder() < 0)
        throw new IOException("Failed to load all chromosomes, missing " + sr.getChr());

      FragmentWritable fw = new FragmentWritable(sr.getChr(), sr.getStart(), sr.getEnd(), sr.getSegmentNum(), sequence);

      context.write(soc, fw);
      }
    }

  static class ChromosomeSequenceReducer extends Reducer<SegmentOrderComparator, FragmentWritable, LongWritable, Text>
    {
    private static final Log log = LogFactory.getLog(ChromosomeSequenceReducer.class);

    private MultipleOutputs mos;
    private List<String> chrs;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      mos = new MultipleOutputs(context);
      chrs = new ArrayList<String>(context.getConfiguration().getStringCollection("chromosomes"));
      if (chrs == null || chrs.size() <= 0)
        throw new IOException("Chromosomes not defined in configuration.");
      log.info("CHROMOSOMES: " + chrs);
      }

    @Override
    protected void reduce(SegmentOrderComparator key, Iterable<FragmentWritable> values, Context context) throws IOException, InterruptedException
      {
      log.debug("ORDER " + key.getOrder() + ":" + key.getSegment());

      Iterator<FragmentWritable> fI = values.iterator();
      while (fI.hasNext())
        {
        FragmentWritable fw = fI.next();
        LongWritable segmentKey = new LongWritable(fw.getSegment());

        String namedOutput = chrs.get((int) key.getOrder());
        mos.write(namedOutput, segmentKey, new Text(fw.getSequence()));
        }

      }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException
      {
      mos.close();
      }

    }


  }
