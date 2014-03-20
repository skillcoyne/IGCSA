/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.database.normal.*;

import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.VariationAdmin;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.SequenceResult;

import org.lcsb.lu.igcsa.hbase.tables.variation.*;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.lcsb.lu.igcsa.variation.fragment.SNV;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.io.IOException;
import java.util.*;

import static org.lcsb.lu.igcsa.hbase.tables.variation.GCBin.*;

/*
NOTE:
This isn't currently being used for anything so it could be that this doesn't work.  It's not been properly tested.
 */
public class MutateFragments extends BWAJob
  {
  private static final Log log = LogFactory.getLog(MutateFragments.class);

  public MutateFragments()
    {
    super(new Configuration());

    Option m = new Option("m", "mutation", true, "Name for mutated genome.");
    m.setRequired(true);
    this.addOptions(m);

    Option p = new Option("p", "parent", true, "Name for parent genome.");
    p.setRequired(true);
    this.addOptions(p);

    Option t = new Option("t", "Test", false, "");
    p.setRequired(false);
    this.addOptions(t);

    }

  @Override
  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());
    if (args.length < 1)
      {
      System.err.println("Usage: " + MutateFragments.class.getSimpleName() + " -p <parent genome> -m <new genome name>");
      System.exit(-1);
      }

    String genome = cl.getOptionValue("m");
    String parent = cl.getOptionValue("p");
    getConf().set("genome", genome);
    getConf().set("parent", parent);

    getConf().setInt("hbase.rpc.timeout",90000);

    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    if (genomeAdmin.getGenomeTable().getGenome(parent) == null)
      {
      System.err.println("Parent genome '" + parent + "' does not exist! Exiting.");
      System.exit(-1);
      }

    if (genomeAdmin.getGenomeTable().getGenome(genome) != null)
      genomeAdmin.deleteGenome(genome);
    genomeAdmin.getGenomeTable().addGenome(genome, parent);

    Job job = new Job(getConf(), "Genome Fragment Mutation");
    job.setJarByClass(MutateFragments.class);

    // this scan will get all sequences for the given genome (so 300 million)
    Scan seqScan = genomeAdmin.getSequenceTable().getScanFor(new Column("info", "genome", parent));
//    if (cl.hasOption("t"))
//      seqScan = genomeAdmin.getSequenceTable().getScanFor(new Column("info", "genome", parent), new Column("loc", "chr", "1"));

    seqScan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
    seqScan.setCacheBlocks(false);

    job.setMapperClass(SequenceTableMapper.class);

    GCBin gcTable = (GCBin) VariationAdmin.getInstance().getTable(VariationTables.GC.getTableName());
    SequenceTableMapper.setMaxGCBins(gcTable.getMaxBins());

    Map<Location, GCResult> bins = new HashMap<Location, GCResult>();
    for (Map.Entry<String, List<GCResult>> entry: gcTable.getBins().entrySet())
      {
      for (GCResult r: entry.getValue())
        bins.put( new Location(entry.getKey(), r.getMin(), r.getMax()), r  );
      }
    SequenceTableMapper.setGCBins(bins);

    TableMapReduceUtil.initTableMapperJob(genomeAdmin.getSequenceTable().getTableName(), seqScan, SequenceTableMapper.class, null, null,
                                          job);

    // because we aren't emitting anything from mapper
    job.setNumReduceTasks(0);
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static class SequenceTableMapper extends TableMapper<ImmutableBytesWritable, Text>
    {
    private static final Log log = LogFactory.getLog(SequenceTableMapper.class);

    private HBaseGenomeAdmin genomeAdmin;
    private VariationAdmin variationAdmin;
    private String genomeName, parentGenome;
    private GenomeResult genome;

    private Map<Character, Probability> snvProbabilities;
    private Map<String, Probability> sizeProbabilities;
    private List<String> variationList;

    private VariationCountPerBin varTable;

    private static Map<String, GCResult> maxGCBins;
    private static Map<Location, GCResult> GCBins;

    public static void setMaxGCBins(Map<String, GCResult> gcBins)
      {
      maxGCBins = gcBins;
      }

    public static void setGCBins(Map<Location, GCResult> bins)
      {
      GCBins = bins;
      }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      if (maxGCBins == null || GCBins == null)
        throw new IOException("GC Bins need to be set up.");

      genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());
      variationAdmin = VariationAdmin.getInstance(context.getConfiguration());

      genomeName = context.getConfiguration().get("genome");
      parentGenome = context.getConfiguration().get("parent");
      genome = genomeAdmin.getGenomeTable().getGenome(genomeName);
      if (genome == null)
        throw new IOException("Genome " + genome + " is missing.");

      try
        {
        SNVProbability table = (SNVProbability) variationAdmin.getTable(VariationTables.SNVP.getTableName());
        snvProbabilities = table.getProbabilities();

        SizeProbability sizeTable = (SizeProbability) variationAdmin.getTable(VariationTables.SIZE.getTableName());
        sizeProbabilities = sizeTable.getProbabilities();
        variationList = sizeTable.getVariationList();
        variationList.add("SNV");
        }
      catch (ProbabilityException e)
        {
        throw new InterruptedException("Failed to start mapper: " + e);
        }

      varTable = (VariationCountPerBin) variationAdmin.getTable(VariationTables.VPB.getTableName());
      }

    private GCResult getBin(String chr, int gcContent)
      {
      for (Location loc: GCBins.keySet())
        {
        if (loc.getChromosome().equals(chr) && loc.containsLocation(gcContent))
          return GCBins.get(loc);
        }
      return null;
      }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
      {
      long start = System.currentTimeMillis();

      final SequenceResult origSeq = genomeAdmin.getSequenceTable().createResult(value);
      final ChromosomeResult origChr = genomeAdmin.getChromosomeTable().getChromosome(parentGenome, origSeq.getChr());

      // get hbase objects for new genome
      ChromosomeResult mutatedChr = genomeAdmin.getChromosomeTable().getChromosome(genome.getName(), origSeq.getChr());
      if (mutatedChr == null)
        {
        String rowId = genomeAdmin.getChromosomeTable().addChromosome(genome, origChr.getChrName(), origChr.getLength(), origChr.getSegmentNumber());
        mutatedChr = genomeAdmin.getChromosomeTable().queryTable(rowId);
        }

      /* --- Mutate sequence --- */
      Random randomFragment = new Random();
      DNASequence mutatedSequence = new DNASequence(origSeq.getSequence());
      /* Don't bother to try and mutate a fragment that is more than 70% 'N' */
      int gcContent = mutatedSequence.calculateGC();
      if (gcContent > (0.3 * origSeq.getSequenceLength()))
        {
        GCResult gcResult = maxGCBins.get(origSeq.getChr());
        if (gcContent < gcResult.getMax())
          getBin(origSeq.getChr(), gcContent);

        // get random fragment within this bin
        List<VCPBResult> varsPerFrag = varTable.getFragment(origSeq.getChr(), gcResult.getMin(), gcResult.getMax(), randomFragment.nextInt(gcResult.getTotalFragments()), variationList);

        Map<Variation, Map<Location, DNASequence>> mutations = new HashMap<Variation, Map<Location, DNASequence>>();

        // apply the variations to the sequence, each of them needs to apply to the same fragment
        // it is possible that one could override another (e.g. a deletion removes SNVs)
        // TODO need to order these by variation...SNV, del, ins, ...
        for (VCPBResult variation : varsPerFrag)
          {
          Variation v = createInstance(variation.getVariationClass());
          v.setVariationName(variation.getVariationName());
          if (variation.getVariationName().equals("SNV"))
            {
            SNV snv = ((SNV) v);
            snv.setSnvFrequencies(snvProbabilities);
            }
          else
            v.setSizeVariation(sizeProbabilities.get(variation.getVariationName()));

          mutatedSequence = v.mutateSequence(mutatedSequence, variation.getVariationCount());
          if (v.getLastMutations().size() > 0)
            mutations.put(v, v.getLastMutations());
          }

        String mutSeqRowId = genomeAdmin.getSequenceTable().addSequence(mutatedChr, origSeq.getStart(), (origSeq.getStart() + mutatedSequence.getLength()), mutatedSequence.getSequence(), origSeq.getSegmentNum());
        if (mutSeqRowId == null)
          throw new IOException("Failed to add sequence.");

        SequenceResult mutSequence = genomeAdmin.getSequenceTable().queryTable(mutSeqRowId);
        for (Variation v : mutations.keySet())
          {
          // add any mutations to the small mutations table -- could do this as a reduce task, might be better as I could do a list of puts
          for (Map.Entry<Location, DNASequence> entry : mutations.get(v).entrySet())
            genomeAdmin.getSmallMutationsTable().addMutation(mutSequence, v, entry.getKey().getStart(), entry.getKey().getEnd(), entry.getValue().getSequence());
          }
        }
      else
        genomeAdmin.getSequenceTable().addSequence(mutatedChr, origSeq.getStart(), origSeq.getEnd(), origSeq.getSequence(), origSeq.getSegmentNum());

      long end = System.currentTimeMillis() - start;
      log.info("FINISHED MAP " + String.valueOf(end) );
      }

    private Variation createInstance(String className)
      {
      try
        {
        return (Variation) Class.forName(className).newInstance();
        }
      catch (ClassNotFoundException e)
        {
        e.printStackTrace();
        }
      catch (InstantiationException e)
        {
        e.printStackTrace();
        }
      catch (IllegalAccessException e)
        {
        e.printStackTrace();
        }
      return null;
      }
    }

  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new MutateFragments(), args);
    }
  }
