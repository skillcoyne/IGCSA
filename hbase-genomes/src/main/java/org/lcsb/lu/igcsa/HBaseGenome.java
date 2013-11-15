/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.avro.generic.GenericData;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.GenomeTableAdmin;
import org.lcsb.lu.igcsa.hbase.rows.*;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HBaseGenome
  {
  static Logger log = Logger.getLogger(HBaseGenome.class.getName());

  private Configuration conf;
  private HBaseAdmin hbaseAdmin;

  private GenomeTable gT;
  private ChromosomeTable cT;
  private SequenceTable sT;
  private SmallMutationsTable smT;
  private KaryotypeIndexTable kiT;
  private KaryotypeTable kT;

  private GenomeResult lastRetrievedGenome;

  public HBaseGenome() throws IOException, MasterNotRunningException
    {
    this(HBaseConfiguration.create());
    }

  public HBaseGenome(Configuration configuration) throws IOException, MasterNotRunningException
    {
    this.conf = configuration;
    this.conf.setInt("timeout", 10);
    this.hbaseAdmin = new HBaseAdmin(conf);
    this.createTables();
    }

  /**
   * @param name
   * @param parentName
   * @param chromosomes
   * @return List of chromosome row ids
   * @throws IOException
   */
  public List<String> addGenome(String name, String parentName, ChromosomeResult... chromosomes) throws IOException
    {
    List<String> chromosomeRowIds = new ArrayList<String>();

    GenomeResult genome = gT.queryTable(name);
    if (genome != null)
      {
      log.warn(name + " genome already exists in the genome table.  Not overwriting");
      for (String c : genome.getChromosomes())
        chromosomeRowIds.add(ChromosomeRow.createRowId(name, c));
      }
    else
      {
      // create genome
      GenomeRow gRow = new GenomeRow(name);
      gRow.addParentColumn(parentName);

      String[] chrs = new String[chromosomes.length];
      for (int i = 0; i < chromosomes.length; i++)
        chrs[i] = chromosomes[i].getChrName();

      gRow.addChromosomeColumn(chrs);
      gT.addRow(gRow);

      // add a chromosome row for each chromosome in the genome
      for (ChromosomeResult chr : chromosomes)
        {
        ChromosomeRow cRow = new ChromosomeRow(ChromosomeRow.createRowId(name, chr.getChrName()));
        cRow.addGenome(name);
        cRow.addChromosomeInfo(chr.getChrName(), chr.getLength(), chr.getSegmentNumber());
        cT.addRow(cRow);

        chromosomeRowIds.add(cRow.getRowIdAsString());
        }
      }

    return chromosomeRowIds;
    }

  /**
   * @param chromosomeRowId
   * @param segmentNumber
   * @param start
   * @param end
   * @param seq
   * @return Sequence row id
   * @throws IOException
   */
  public String addSequence(String chromosomeRowId, int segmentNumber, int start, int end, String seq) throws IOException
    {
    ChromosomeResult chromosome = cT.queryTable(chromosomeRowId);

    SequenceRow sRow = new SequenceRow(SequenceRow.createRowId(chromosome.getGenomeName(), chromosome.getChrName(), segmentNumber));
    sRow.addBasePairs(seq);
    sRow.addLocation(chromosome.getChrName(), segmentNumber, start, end);
    sRow.addGenome(chromosome.getGenomeName());

    sT.addRow(sRow);

    return sRow.getRowIdAsString();
    }

  /**
   * @param sequenceRowId
   * @param start
   * @param end
   * @param mutation
   * @param seq
   * @return Small mutation table row id
   * @throws IOException
   */
  public String addSmallMutation(String sequenceRowId, int start, int end, SmallMutationRow.SmallMutation mutation,
                                 String seq) throws IOException
    {
    SequenceResult sequence = sT.queryTable(sequenceRowId);

    SmallMutationRow smRow = new SmallMutationRow(SmallMutationRow.createRowId(sequence.getGenome(), sequence.getChr(),
                                                                               sequence.getSegment(), start));
    smRow.addGenomeInfo(sequence.getGenome(), mutation);
    smRow.addLocation(sequence.getChr(), sequence.getSegment(), start, end);
    smRow.addSequence(seq);

    smT.addRow(smRow);

    return smRow.getRowIdAsString();
    }

  /**
   * @param genome
   * @param aberrations
   * @return Karyotype aberration row ids (one per aberration, multiple per genome).
   * @throws IOException
   */
  public List<String> addAberration(String genome, List<String> aberrations) throws IOException
    {
    if (gT.queryTable(genome) == null) throw new IllegalArgumentException("No genome named '" + genome + "' defined in the genome table.");

    KaryotypeIndexRow kRow = new KaryotypeIndexRow(genome);
    kRow.addAberrations(aberrations.toArray(new String[aberrations.size()]));

    Pattern p = Pattern.compile("(\\d+|X|Y):(\\d+-\\d+)");
    Set<String> expectedIds = new HashSet<String>(kRow.getKaryotypeTableRowIds());
    for (String abr : aberrations)
      {
      KaryotypeRow karyotypeRow = new KaryotypeRow(KaryotypeRow.createRowId(genome, abr));
      if (!expectedIds.contains(karyotypeRow)) throw new IOException("Karyotype ids created incorrectly???"); // should not happen

      Matcher m = p.matcher(abr);
      MatchResult mr = m.toMatchResult();

      String type = abr.substring(0, abr.indexOf("("));

      List<String[]> separatedAberrations = new ArrayList<String[]>();
      for (int i = 1; i <= mr.groupCount(); i += 2)
        {
        String chr = mr.group(i);
        String loc = mr.group(i + 1);

        separatedAberrations.add(new String[]{chr, loc});
        }

      karyotypeRow.addAberration(type, separatedAberrations);

      kT.addRow(karyotypeRow);
      }

    kiT.addRow(kRow);

    return kRow.getKaryotypeTableRowIds();
    }

  public List<GenomeResult> retrieveGenomes() throws IOException
    {
    return gT.getRows();
    }

  public GenomeResult retrieveGenome(String genomeName) throws IOException
    {
    GenomeResult genome = gT.queryTable(genomeName);
    this.lastRetrievedGenome = genome;
    return genome;
    }


  /**
   * @param genomeName
   * @return All aberrations
   * @throws IOException
   */
  public List<KaryotypeResult> retrieveKaryotype(String genomeName) throws IOException
    {
    KaryotypeIndexTable.KaryotypeIndexResult result = kiT.queryTable(genomeName);

    List<KaryotypeResult> karyotypeResults = new ArrayList<KaryotypeResult>();
    for (String abr : result.getAberrations())
      karyotypeResults.add(kT.queryTable(KaryotypeRow.createRowId(genomeName, abr)));

    return karyotypeResults;
    }


  /**
   * From last queried genome.
   *
   * @return
   * @throws IOException
   */
  public List<KaryotypeResult> retrieveKaryotype() throws IOException
    {
    return retrieveKaryotype(this.lastRetrievedGenome.getName());
    }


  /**
   * Retrieve chromosomes from the last retrieved genome
   *
   * @return
   * @throws IOException
   */
  public List<ChromosomeResult> retrieveChromosomes() throws IOException
    {
    return retrieveChromosomes(this.lastRetrievedGenome);
    }

  public List<ChromosomeResult> retrieveChromosomes(GenomeResult genome) throws IOException
    {
    Map<GenomeResult, List<ChromosomeResult>> data = new HashMap<GenomeResult, List<ChromosomeResult>>();
    // associated chromosomes
    List<ChromosomeResult> chromosomes = new ArrayList<ChromosomeResult>();

    for (String chr : genome.getChromosomes())
      {
      ChromosomeResult chromosome = cT.queryTable(ChromosomeRow.createRowId(genome.getName(), chr));
      chromosomes.add(chromosome);
      }

    return chromosomes;
    }


  public List<SequenceResult> retrieveSequences(ChromosomeResult chromosome) throws IOException
    {
    List<SequenceResult> sequences = new ArrayList<SequenceResult>();
    for (int i = 1; i < chromosome.getSegmentNumber(); i++)
      sequences.add(sT.queryTable(SequenceRow.createRowId(chromosome.getGenomeName(), chromosome.getChrName(), i)));

    return sequences;
    }

  public List<SmallMutationsResult> retrieveMutations(SequenceResult segment) throws IOException
    {
    List<SmallMutationsResult> mutations = new ArrayList<SmallMutationsResult>();
    for (int i = 1; i <= segment.getSequenceLength(); i++)
      mutations.add(smT.queryTable(SmallMutationRow.createRowId(segment.getGenome(), segment.getChr(), segment.getSegment(), i)));

    return mutations;
    }

  /**
   * Gets the sequences that overlap the aberration locations
   *
   * @param karyotypeResult
   */
  public void retrieveSequence(KaryotypeResult karyotypeResult)
    {

    for (KaryotypeResult.AberrationLocation loc : karyotypeResult.getAberrationDefinitions())
      {
      //      sT.
      //
      //      loc.getStart()
      }

    }


  public void closeConections() throws IOException
    {
    hbaseAdmin.close();
    }

  public void disableDatabases() throws IOException
    {
    hbaseAdmin.disableTable("genome");
    hbaseAdmin.disableTable("chromosome");
    hbaseAdmin.disableTable("sequence");
    hbaseAdmin.disableTable("small_mutations");
    hbaseAdmin.disableTable("karyotype_index");
    hbaseAdmin.disableTable("karyotype");
    }

  private void createTables()
    {
    boolean create = false;
    try
      {
      if (hbaseAdmin.getTableNames().length < 6)
        {
        log.info("Creating tables");
        create = true;
        }
      // I should really just instantiate these as needed
      gT = new GenomeTable(this.conf, this.hbaseAdmin, "genome", create);
      cT = new ChromosomeTable(this.conf, this.hbaseAdmin, "chromosome", create);
      sT = new SequenceTable(this.conf, this.hbaseAdmin, "sequence", create);
      smT = new SmallMutationsTable(this.conf, this.hbaseAdmin, "small_mutations", create);
      kiT = new KaryotypeIndexTable(this.conf, this.hbaseAdmin, "karytoype_index", create);
      kT = new KaryotypeTable(this.conf, this.hbaseAdmin, "karyotype", create);
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }

    }


  }
