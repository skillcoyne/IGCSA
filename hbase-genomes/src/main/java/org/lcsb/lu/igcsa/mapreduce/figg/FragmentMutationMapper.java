package org.lcsb.lu.igcsa.mapreduce.figg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.VariationAdmin;
import org.lcsb.lu.igcsa.hbase.rows.Row;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.SequenceResult;
import org.lcsb.lu.igcsa.hbase.tables.variation.*;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.variation.fragment.SNV;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.io.IOException;
import java.util.*;


/**
 * org.lcsb.lu.igcsa.mapreduce.figg
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FragmentMutationMapper extends TableMapper<ImmutableBytesWritable, Row>
  {
  private static final Log log = LogFactory.getLog(FragmentMutationMapper.class);

  private HBaseGenomeAdmin genomeAdmin;
  private VariationAdmin variationAdmin;
  private String genomeName, parentGenome;
  private GenomeResult genome;

  private Map<Character, Probability> snvProbabilities;
  private Map<String, Probability> sizeProbabilities;
  private List<String> variationList;

  private VariationCountPerBin varTable;

  private static Map<String, GCBin.GCResult> maxGCBins;
  private static Map<Location, GCBin.GCResult> GCBins;

  private void setupGCBins(Configuration conf) throws IOException
    {
    GCBin gcTable = (GCBin) VariationAdmin.getInstance(conf).getTable(VariationTables.GC.getTableName());

    GCBins = new HashMap<Location, GCBin.GCResult>();
    for (Map.Entry<String, List<GCBin.GCResult>> entry : gcTable.getBins().entrySet())
      {
      for (GCBin.GCResult r : entry.getValue())
        GCBins.put(new Location(entry.getKey(), r.getMin(), r.getMax()), r);
      }

    maxGCBins = gcTable.getMaxBins();
    }

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    super.setup(context);
    setupGCBins(context.getConfiguration());

    genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());
    variationAdmin = VariationAdmin.getInstance(context.getConfiguration());

    genomeName = context.getConfiguration().get("genome");
    parentGenome = context.getConfiguration().get("parent");
    genome = genomeAdmin.getGenomeTable().getGenome(genomeName);
    if (genome == null) throw new IOException("Genome " + genome + " is missing.");

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

  private GCBin.GCResult getBin(String chr, int gcContent)
    {
    for (Location loc : GCBins.keySet())
      {
      if (loc.getChromosome().equals(chr) && loc.containsLocation(gcContent)) return GCBins.get(loc);
      }
    return null;
    }

  @Override
  protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
    {
    if (maxGCBins == null || GCBins == null) throw new IOException("GC Bins need to be set up.");

    long start = System.currentTimeMillis();

    final SequenceResult origSeq = genomeAdmin.getSequenceTable().createResult(value);
    final ChromosomeResult origChr = genomeAdmin.getChromosomeTable().getChromosome(parentGenome, origSeq.getChr());

    // get hbase objects for new genome
    ChromosomeResult mutatedChr = genomeAdmin.getChromosomeTable().getChromosome(genome.getName(), origSeq.getChr());
    if (mutatedChr == null)
      {
      String rowId = genomeAdmin.getChromosomeTable().addChromosome(genome, origChr.getChrName(), origChr.getLength(),
                                                                    origChr.getSegmentNumber());
      mutatedChr = genomeAdmin.getChromosomeTable().queryTable(rowId);
      }

    /* --- Mutate sequence --- */
    Random randomFragment = new Random();
    DNASequence mutatedSequence = new DNASequence(origSeq.getSequence());
    /* Don't bother to try and mutate a fragment that is more than 70% 'N' */
    int gcContent = mutatedSequence.calculateGC();
    if (gcContent > (0.3 * origSeq.getSequenceLength()))
      {
      GCBin.GCResult gcResult = maxGCBins.get(origSeq.getChr());
      if (gcContent < gcResult.getMax()) getBin(origSeq.getChr(), gcContent);

      // get random fragment within this bin
      List<VCPBResult> varsPerFrag = varTable.getFragment(origSeq.getChr(), gcResult.getMin(), gcResult.getMax(),
                                                          randomFragment.nextInt(gcResult.getTotalFragments()), variationList);

      Map<Variation, Map<Location, DNASequence>> mutations = new HashMap<Variation, Map<Location, DNASequence>>();

      // apply the variations to the sequence, each of them needs to apply to the same fragment
      // it is possible that one could override another (e.g. a deletion removes SNVs)
      // TODO need to check that these are ordered these by variation based on the hbase ordering...SNV, del, ins, ...
      for (VCPBResult variation : varsPerFrag)
        {
        Variation v = createInstance(variation.getVariationClass());
        v.setVariationName(variation.getVariationName());
        if (variation.getVariationName().equals("SNV"))
          {
          SNV snv = ((SNV) v);
          snv.setSnvFrequencies(snvProbabilities);
          }
        else v.setSizeVariation(sizeProbabilities.get(variation.getVariationName()));

        mutatedSequence = v.mutateSequence(mutatedSequence, variation.getVariationCount());
        if (v.getLastMutations().size() > 0) mutations.put(v, v.getLastMutations());
        }

      String mutSeqRowId = genomeAdmin.getSequenceTable().addSequence(mutatedChr, origSeq.getStart(),
                                                                      (origSeq.getStart() + mutatedSequence.getLength()),
                                                                      mutatedSequence.getSequence(), origSeq.getSegmentNum());
      if (mutSeqRowId == null) throw new IOException("Failed to add sequence.");

      // add any mutations to the small mutations table, best done as a batch job
      List<Put> puts = new ArrayList<Put>();
      SequenceResult mutSequence = genomeAdmin.getSequenceTable().queryTable(mutSeqRowId);
      for (Variation v : mutations.keySet())
        {
        for (Map.Entry<Location, DNASequence> entry : mutations.get(v).entrySet())
          {
          try
            {
            Row row = genomeAdmin.getSmallMutationsTable().newMutationRow(mutSequence, v, entry.getKey().getStart(),
                                                                          entry.getKey().getEnd(), entry.getValue().getSequence());
            puts.add(new Put(genomeAdmin.getSmallMutationsTable().getPut(row)));
            }
          catch (IllegalArgumentException ae)
            {
            log.error("Failed to add " + mutSeqRowId + " var " + v.toString(), ae);
            }
          }
        }
      genomeAdmin.getSmallMutationsTable().put(puts);
      }
    else
      genomeAdmin.getSequenceTable().addSequence(mutatedChr, origSeq.getStart(), origSeq.getEnd(), origSeq.getSequence(),
                                                 origSeq.getSegmentNum());

    long end = System.currentTimeMillis() - start;
    log.debug("FINISHED MAP " + origSeq.getRowId() + " time=" + String.valueOf(end));
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
