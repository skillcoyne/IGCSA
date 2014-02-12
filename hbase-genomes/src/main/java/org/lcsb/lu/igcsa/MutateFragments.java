/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;

import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.HBaseSequence;
import org.lcsb.lu.igcsa.hbase.tables.Column;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;

import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.*;

/*
NOTE:
This isn't currently being used for anything so it could be that this doesn't work.  It's not been properly tested.
 */
public class MutateFragments extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(MutateFragments.class);

  static ClassPathXmlApplicationContext springContext;

  public MutateFragments()
    {
    super(HBaseConfiguration.create());
    springContext = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml"});
    if (springContext == null)
      throw new RuntimeException("Failed to load Spring application context");
    }

  @Override
  public int run(String[] args) throws Exception
    {
    String genome = args[0];
    String parent = args[1];

    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());

    // don't think I need these
    getConf().set("genome", genome);
    getConf().set("parent", parent);

    genomeAdmin.deleteGenome(genome);

    // make sure the new genome is created before we start
    new HBaseGenome(genome, parent);

    Job job = new Job(getConf(), "Reference Genome Fragmentation");
    job.setJarByClass(MutateFragments.class);

    // this scan will get all sequences for the given genome (so 300 million)
    Scan seqScan = genomeAdmin.getSequenceTable().getScanFor(new Column("info", "genome", "GRCh37"));
    seqScan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
    seqScan.setCacheBlocks(false);

    job.setMapperClass(SequenceTableMapper.class);
    TableMapReduceUtil.initTableMapperJob(genomeAdmin.getSequenceTable().getTableName(), seqScan, SequenceTableMapper.class, null, null, job);

    // because we aren't emitting anything from mapper
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static class SequenceTableMapper extends TableMapper<ImmutableBytesWritable, Text>
    {
    private String genomeName;
    private String parentName;
    private VariantUtils variantUtils;
    private GCBinDAO binDAO;
    private FragmentDAO fragmentDAO;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      genomeName = context.getConfiguration().get("genome");
      parentName = context.getConfiguration().get("parent");
      variantUtils = (VariantUtils) springContext.getBean("variantUtils");
      binDAO = (GCBinDAO) springContext.getBean("GCBinDAO");
      fragmentDAO = (FragmentDAO) springContext.getBean("FragmentDAO");
      }


    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
      {
      SequenceResult seq = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);

      log.info(seq.getChr() + " " + seq.getStart() + "-" + seq.getEnd());

      // get hbase objects for new genome
      HBaseGenome genome = HBaseGenomeAdmin.getHBaseGenomeAdmin().getGenome(genomeName);
      HBaseChromosome chromosome = genome.getChromosome(seq.getChr());
      if (chromosome == null)
        chromosome = genome.addChromosome(seq.getChr(), 0, 0);

      Random randomFragment = new Random();
      DNASequence mutatedSequence = new DNASequence(seq.getSequence());
      /* Don't bother to try and mutate a fragment that is more than 70% 'N' */
      if (mutatedSequence.calculateNucleotides() > (0.3 * seq.getSequenceLength()))
        {
        Bin gcBin = this.binDAO.getBinByGC(seq.getChr(), mutatedSequence.calculateGC());
        // TODO get random fragment within the GC bin for each variation -- this is the slowest query!!!
        Map<String, Fragment> fragmentVarMap = fragmentDAO.getFragment(seq.getChr(), gcBin.getBinId(), randomFragment.nextInt(gcBin.getSize()));

        Map<Variation, Map<Location, DNASequence>> mutations = new HashMap<Variation, Map<Location, DNASequence>>();

        // apply the variations to the sequence, each of them needs to apply to the same fragment
        // it is possible that one could override another (e.g. a deletion removes SNVs)
        for (Variation variation : getVariants(seq.getChr()))
          {
          Fragment fragment = fragmentVarMap.get(variation.getVariationName());
          variation.setMutationFragment(fragment);

          mutatedSequence = variation.mutateSequence(mutatedSequence);

          if (variation.getLastMutations().size() > 0)
            mutations.put(variation, variation.getLastMutations());
          }

        /*
        NOTE: I could cut down on the size of the HBase (and actually use derby or something similar) by keeping only the actual segments from the original reference
        all subsequent mutations could be applied at runtime when I generate a FASTA file.
         */
        HBaseSequence hBaseSequence = chromosome.addSequence(seq.getStart(),
            seq.getStart() + mutatedSequence.getLength(),
            mutatedSequence.getSequence(),
            seq.getSegmentNum());

        for (Variation v : mutations.keySet())
          {
          // add any mutations to the small mutations table
          for (Map.Entry<Location, DNASequence> entry : mutations.get(v).entrySet())
            hBaseSequence.addSmallMutation(v, entry.getKey().getStart(), entry.getKey().getEnd(), entry.getValue().getSequence());
          }
        }
      else
        chromosome.addSequence(seq.getStart(), seq.getEnd(), seq.getSequence(), seq.getSegmentNum());
      }

    private List<Variation> getVariants(String chr)
      {
      try
        {
        return variantUtils.getVariantList(chr);
        }
      catch (IllegalAccessException e)
        {
        e.printStackTrace();
        }
      catch (ProbabilityException e)
        {
        e.printStackTrace();
        }
      catch (InstantiationException e)
        {
        e.printStackTrace();
        }
      return new ArrayList<Variation>();
      }

    }


//  public static void main(String[] args) throws Exception
//    {
//    String genome = "igcsa2", parent = "GRCh37";
//    long start = System.currentTimeMillis();
//    ToolRunner.run(new MutateFragments(), new String[]{genome, parent});
//    long end = System.currentTimeMillis() - start;
//
//    log.info("Finished mutations for " + genome + ": " + (end/1000) );
//    }
  }
