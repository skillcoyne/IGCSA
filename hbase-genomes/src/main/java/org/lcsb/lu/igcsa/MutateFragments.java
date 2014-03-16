/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

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

  //private ClassPathXmlApplicationContext springContext;

  public MutateFragments()
    {
    super(new Configuration());
    //    springContext = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml"});
    //    if (springContext == null)
    //      throw new RuntimeException("Failed to load Spring application context");

    //springContext.get
    // hardcoded to test only

    //    try
    //      {
    //      this.addArchive(new URI("/derby/normal_variation.tgz#db"));
    //      }
    //    catch (URISyntaxException e)
    //      {
    //      e.printStackTrace();
    //      }

    Option m = new Option("m", "mutation", true, "Name for mutated genome.");
    m.setRequired(true);
    this.addOptions(m);

    Option p = new Option("p", "parent", true, "Name for parent genome.");
    p.setRequired(true);
    this.addOptions(p);
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

    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    if (genomeAdmin.getGenomeTable().getGenome(parent) == null)
      {
      System.err.println("Parent genome '" + parent + "' does not exist! Exiting.");
      System.exit(-1);
      }

    if (genomeAdmin.getGenomeTable().getGenome(genome) != null)
      genomeAdmin.getGenomeTable().delete(genome);

    genomeAdmin.getGenomeTable().addGenome(genome, parent);

    Job job = new Job(getConf(), "Reference Genome Fragmentation");
    job.setJarByClass(MutateFragments.class);

    // this scan will get all sequences for the given genome (so 300 million)
    Scan seqScan = genomeAdmin.getSequenceTable().getScanFor(new Column("info", "genome", parent));
    seqScan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
    seqScan.setCacheBlocks(false);

    //SequenceTableMapper.setSpringContext(springContext);
    job.setMapperClass(SequenceTableMapper.class);
    TableMapReduceUtil.initTableMapperJob(genomeAdmin.getSequenceTable().getTableName(), seqScan, SequenceTableMapper.class, null, null, job);

    // because we aren't emitting anything from mapper
    job.setOutputFormatClass(NullOutputFormat.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static class SequenceTableMapper extends TableMapper<ImmutableBytesWritable, Text>
    {
    private static final Log log = LogFactory.getLog(SequenceTableMapper.class);

    private HBaseGenomeAdmin admin;
    private VariationAdmin variationAdmin;
    private String genomeName;
    private GenomeResult genome;

    private Map<Character, Probability> snvProbabilities;
    private Map<String, Probability> sizeProbabilities;
    private List<String> variationList;
//    private VariantUtils variantUtils;
//    private GCBinDAO binDAO;
//    private FragmentDAO fragmentDAO;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);

      admin = HBaseGenomeAdmin.getHBaseGenomeAdmin();
      variationAdmin = VariationAdmin.getInstance();

      genomeName = context.getConfiguration().get("genome");
      genome = admin.getGenomeTable().getGenome(genomeName);
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
        throw new InterruptedException("Failed to startup mapper: " + e);
        }

      }


    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
      {
      SequenceResult seq = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(value);

      // get hbase objects for new genome
      ChromosomeResult mutatedChr = admin.getChromosomeTable().getChromosome(seq.getGenome(), seq.getChr());
      if (mutatedChr == null)
        {
        String rowId = admin.getChromosomeTable().addChromosome(genome, seq.getChr(), 0, 0);
        mutatedChr = admin.getChromosomeTable().queryTable(rowId);
        }

      Random randomFragment = new Random();
      DNASequence mutatedSequence = new DNASequence(seq.getSequence());
      /* Don't bother to try and mutate a fragment that is more than 70% 'N' */
      if (mutatedSequence.calculateNucleotides() > (0.3 * seq.getSequenceLength()))
        {
        GCBin gcTable = (GCBin) variationAdmin.getTable(VariationTables.GC.getTableName());
        GCResult gcResult = gcTable.getBinFor(seq.getChr(), mutatedSequence.calculateGC());

        // get random fragment within this bin
        VariationCountPerBin table = (VariationCountPerBin) variationAdmin.getTable(VariationTables.VPB.getTableName());
        List<VCPBResult> varsPerFrag = table.getFragment(seq.getChr(), gcResult.getMin(), gcResult.getMax(),
                                                         randomFragment.nextInt(gcResult.getTotalFragments()), variationList);

        Map<Variation, Map<Location, DNASequence>> mutations = new HashMap<Variation, Map<Location, DNASequence>>();

        // apply the variations to the sequence, each of them needs to apply to the same fragment
        // it is possible that one could override another (e.g. a deletion removes SNVs)

        // TODO need to order these by variation...?
        for (VCPBResult variation: varsPerFrag)
          {
          Variation v = createInstance(variation.getVariationClass());
          if (variation.getVariationName().equals("SNV"))
            {
            SNV snv = ((SNV) v);
            snv.setSnvFrequencies(snvProbabilities);
            }
          else
            {
            v.setVariationName(variation.getVariationName());
            v.setSizeVariation( sizeProbabilities.get(variation.getVariationName()) );
            }
          mutatedSequence = v.mutateSequence(mutatedSequence, variation.getVariationCount());
          if (v.getLastMutations().size() > 0)
            mutations.put(v, v.getLastMutations());
          }
        /*
        NOTE: I could cut down on the size of the HBase (and actually use derby or something similar) by keeping only the actual segments
         from the original reference
        all subsequent mutations could be applied at runtime when I generate a FASTA file.
         */
        String mutSeqRowId = admin.getSequenceTable().addSequence(mutatedChr, seq.getStart(), (seq.getStart() + mutatedSequence.getLength()), mutatedSequence.getSequence(), seq.getSegmentNum());
        if (mutSeqRowId == null)
          throw new IOException("Failed to add sequence.");

        SequenceResult mutSequence = admin.getSequenceTable().queryTable(mutSeqRowId);
        for (Variation v : mutations.keySet())
          {
          // add any mutations to the small mutations table
          for (Map.Entry<Location, DNASequence> entry : mutations.get(v).entrySet())
            admin.getSmallMutationsTable().addMutation(mutSequence, v, entry.getKey().getStart(), entry.getKey().getEnd(), entry.getValue().getSequence());

          }
        }
      else
        admin.getSequenceTable().addSequence(mutatedChr, seq.getStart(), seq.getEnd(), seq.getSequence(), seq.getSegmentNum());
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
    log.info(args);
    long start = System.currentTimeMillis();
    ToolRunner.run(new MutateFragments(), args);
    long end = System.currentTimeMillis() - start;

    log.info("Finished mutations " + (end / 1000));
    }
  }
