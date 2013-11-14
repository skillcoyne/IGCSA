/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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


  public HBaseGenome() throws IOException, MasterNotRunningException
    {
    this(HBaseConfiguration.create());
    }

  public HBaseGenome(Configuration configuration) throws IOException, MasterNotRunningException
    {
    conf.setInt("timeout", 10);
    hbaseAdmin = new HBaseAdmin(conf);
    this.createTables();
    }

  /**
   * @param name
   * @param parentName
   * @param chromosomes
   * @return List of chromosome row ids
   * @throws IOException
   */
  public List<String> addGenome(String name, String parentName, List<ChromosomeResult> chromosomes) throws IOException
    {
    List<String> chromosomeRowIds = new ArrayList<String>();

    // create genome
    GenomeRow gRow = new GenomeRow(name);
    if (parentName != null)
      gRow.addParentColumn(parentName);

    String[] chrs = new String[chromosomes.size()];
    for (int i = 0; i < chromosomes.size(); i++)
      chrs[i] = chromosomes.get(i).getChrName();

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
  public String addSmallMutation(String sequenceRowId, int start, int end, SmallMutationRow.SmallMutation mutation, String seq) throws IOException
    {
    SequenceResult sequence = sT.queryTable(sequenceRowId);

    SmallMutationRow smRow = new SmallMutationRow(SmallMutationRow.createRowId(sequence.getGenome(), sequence.getChr(), sequence.getSegment(), start));
    smRow.addGenomeInfo(sequence.getGenome(), mutation);
    smRow.addLocation(sequence.getChr(), sequence.getSegment(), start, end);
    if (seq != null) smRow.addSequence(seq);

    sT.addRow(smRow);

    return smRow.getRowIdAsString();
    }

  /**
   *
   * @param genome
   * @param aberrations
   * @return Karyotype aberration row ids (one per aberration, multiple per genome).
   * @throws IOException
   */
  public List<String> addAberration(String genome, List<String> aberrations) throws IOException
    {
    if (gT.queryTable(genome) == null)
      throw new IllegalArgumentException("No genome named '" + genome + "' defined in the genome table.");

    KaryotypeIndexRow kRow = new KaryotypeIndexRow(genome);
    kRow.addAberrations(aberrations.toArray(new String[aberrations.size()]));

    Pattern p = Pattern.compile("(\\d+|X|Y):(\\d+-\\d+)");
    Set<String> expectedIds = new HashSet<String>(kRow.getKaryotypeTableRowIds());
    for (String abr: aberrations)
      {
      KaryotypeRow karyotypeRow = new KaryotypeRow( KaryotypeRow.createRowId(genome, abr) );
      if (!expectedIds.contains(karyotypeRow))
        throw new IOException("Karyotype ids created incorrectly???"); // should not happen

      Matcher m = p.matcher(abr);
      MatchResult mr = m.toMatchResult();

      String type = abr.substring(0, abr.indexOf("("));

      List<String[]> separatedAberrations = new ArrayList<String[]>();
      for (int i=1; i<=mr.groupCount(); i+=2)
        {
        String chr = mr.group(i);
        String loc = mr.group(i+1);

        separatedAberrations.add(new String[]{chr, loc});
        }

      karyotypeRow.addAberration(type, separatedAberrations);

      kT.addRow(karyotypeRow);
      }

    kiT.addRow(kRow);

    return kRow.getKaryotypeTableRowIds();
    }


  private void createTables() throws IOException
    {
    gT = new GenomeTable(this.conf, this.hbaseAdmin, "genome", true);
    cT = new ChromosomeTable(this.conf, this.hbaseAdmin, "chromosome", true);
    sT = new SequenceTable(this.conf, this.hbaseAdmin, "sequence", true);
    smT = new SmallMutationsTable(this.conf, this.hbaseAdmin, "small_mutations", true);
    kiT = new KaryotypeIndexTable(this.conf, this.hbaseAdmin, "karytoype_index", true);
    kT = new KaryotypeTable(this.conf, this.hbaseAdmin, "karyotype", true);
    }


  }
