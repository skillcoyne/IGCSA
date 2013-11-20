/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;


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
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FragmentKey;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.lcsb.lu.igcsa.variation.fragment.Variation;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.*;

public class MutateFragments extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(MutateFragments.class.getName());

  static ClassPathXmlApplicationContext springContext;

  public MutateFragments()
    {
    springContext = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml"});
    if (springContext == null)
      throw new RuntimeException("Failed to load Spring application context");
    }


  @Override
  public int run(String[] strings) throws Exception
    {
    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    config.set("genome", "igcsa1");
    config.set("parent", "GRCh37");
    //    config.set("genome", "GRCh37");
    //    config.set("chromosome", chr);

    HBaseGenome genome = new HBaseGenome("igcsa1", "GRCh37");


    Job job = new Job(config, "Reference Genome Fragmentation");

    job.setJarByClass(MutateFragments.class);

    //    job.setMapOutputKeyClass(ImmutableBytesWritable.class);
    //    job.setMapOutputValueClass(Text.class);

    //    job.setOutputKeyClass(ImmutableBytesWritable.class);
    //    job.setOutputValueClass(Text.class);

    //job.setInputFormatClass(FASTAInputFormat.class);

    //FileInputFormat.addInputPath(job, new Path(fastaPath));
    //FileOutputFormat.setOutputPath(job, new Path("/tmp/figg2/chr" + chr));

    Scan scan = genomeAdmin.getSequenceTable().getScanFor(new Column("info", "genome", "GRCh37"));
    scan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
    scan.setCacheBlocks(false);

    job.setMapperClass(GenomeTableMapper.class);
    TableMapReduceUtil.initTableMapperJob(genomeAdmin.getSequenceTable().getTableName(), scan, GenomeTableMapper.class, null, null, job);

    job.setOutputFormatClass(NullOutputFormat.class);   // because we aren't emitting anything from mapper


    //    job.setReducerClass(FragmentReducer.class);
    // output to a table
    //TableMapReduceUtil.initTableReducerJob(genomeAdmin.getSequenceTable().getTableName(), FragmentReducer.class, job);

    job.submit();


    return 0;
    }


  public static class GenomeTableMapper extends TableMapper<ImmutableBytesWritable, Text>
    {
    private String genomeName;
    private VariantUtils variantUtils;
    private GCBinDAO binDAO;
    private FragmentDAO fragmentDAO;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      genomeName = context.getConfiguration().get("genome");
      variantUtils = (VariantUtils) springContext.getBean("variantUtils");
      binDAO = (GCBinDAO) springContext.getBean("GCBinDAO");
      fragmentDAO = (FragmentDAO) springContext.getBean("FragmentDAO");
      }


    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
      {
      SequenceResult seq = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);

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
        Bin gcBin = this.binDAO.getBinByGC(seq.getChr(), mutatedSequence.calculateGC()); // TODO some of these calls take > 20ms (not even most though)
        // get random fragment within the GC bin for each variation
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

        HBaseSequence hBaseSequence = chromosome.addSequence(seq.getStart(), seq.getEnd(), seq.getSegment(), seq.getSequence());
        for (Variation v : mutations.keySet())
          {
          for (Map.Entry<Location, DNASequence> entry : mutations.get(v).entrySet())
            hBaseSequence.addSmallMutation(v, entry.getKey().getStart(), entry.getKey().getEnd(), entry.getValue().getSequence());
          }
        }
      else
        chromosome.addSequence(seq.getStart(), seq.getEnd(), seq.getSegment(), seq.getSequence());

      log.info("foo");

      //super.map(key, value, context);
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


    //    @Override
    //    protected void map(ImmutableBytesWritable key, Text value, Context context) throws IOException, InterruptedException
    //      {
    //      //super.map(key, value, context);
    //      HBaseGenome genome = HBaseGenomeAdmin.getHBaseGenomeAdmin().getGenome(genomeName);
    //      }

    }


  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new MutateFragments(), args);
    }
  }
