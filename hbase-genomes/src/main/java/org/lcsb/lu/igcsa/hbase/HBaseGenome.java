/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.MinimalKaryotype;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.rows.GenomeRow;
import org.lcsb.lu.igcsa.hbase.rows.KaryotypeIndexRow;
import org.lcsb.lu.igcsa.hbase.rows.KaryotypeRow;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.io.IOException;
import java.util.*;

public class HBaseGenome extends HBaseConnectedObjects
  {
  static Logger log = Logger.getLogger(HBaseGenome.class.getName());

  private GenomeResult genome;

  private GenomeTable gT;
  private ChromosomeTable cT;
  private KaryotypeTable kT;
  private KaryotypeIndexTable kiT;

  protected HBaseGenome(GenomeResult result) throws IOException
    {
    super(result);
    this.genome = result;
    }

  protected HBaseGenome(String rowId) throws IOException
    {
    super(rowId);
    this.genome = this.gT.queryTable(rowId);
    }

  public HBaseGenome(String genomeName, String parentGenome) throws IOException
    {
    super(genomeName);

    if (parentGenome != null && this.gT.queryTable(parentGenome) == null)
      throw new IOException("No genome matching parent: " + parentGenome);

    this.genome = this.gT.queryTable(genomeName);
    if (genome == null)
      {
      GenomeRow row = new GenomeRow(genomeName);
      row.addParentColumn(parentGenome);
      gT.addRow(row);

      this.genome = this.gT.queryTable(genomeName);
      }

    }

  public HBaseChromosome updateChromosome(String chr, int length, int numSegments) throws IOException
    {
    GenomeResult result = gT.queryTable(genome.getName(), new Column("chr", "list"));

    List<String> currentChrs = result.getChromosomes();
    if (!currentChrs.contains(chr))
      return addChromosome(chr, length, numSegments);

    ChromosomeResult cR = cT.queryTable(ChromosomeRow.createRowId(genome.getName(), chr));

    ChromosomeRow updatedRow = new ChromosomeRow(ChromosomeRow.createRowId(genome.getName(), chr));
    updatedRow.addChromosomeInfo(chr, length, numSegments);
    updatedRow.addGenome(cR.getGenomeName());

    cT.addRow(updatedRow);

    return new HBaseChromosome(cT.queryTable(updatedRow.getRowIdAsString()));
    }


  public HBaseChromosome addChromosome(String chr, int length, int numSegments) throws IOException
    {
    GenomeResult result = gT.queryTable(genome.getName());

    List<String> currentChrs = result.getChromosomes();
    if (currentChrs.contains(chr))
      log.warn("Chromosome " + chr + " exists. Not overwriting.");
    else
      {
      currentChrs.add(chr);

      gT.updateRow(genome.getName(), new Column("chr", "list", StringUtils.join(currentChrs.iterator(), ",")));

      ChromosomeRow row = new ChromosomeRow(ChromosomeRow.createRowId(genome.getName(), chr));
      row.addGenome(genome.getName());
      row.addChromosomeInfo(chr, length, numSegments);

      cT.addRow(row);

      return new HBaseChromosome(cT.queryTable(row.getRowIdAsString()));
      }

    return new HBaseChromosome(cT.queryTable(ChromosomeRow.createRowId(genome.getName(), chr)));
    }

  public HBaseChromosome getChromosome(String chr) throws IOException
    {
    ChromosomeResult result = cT.queryTable(ChromosomeRow.createRowId(genome.getName(), chr));
    return (result != null)? new HBaseChromosome(result): null;
    }

  public void createKaryotype(String baseKaryotypeName, String parentGenome, List<MinimalKaryotype> karyotypes) throws IOException
    {
    int i = 0;
    for (MinimalKaryotype kt: karyotypes)
      {
      ++i;
      String karyotypeName = baseKaryotypeName+i;
      KaryotypeIndexRow row = new KaryotypeIndexRow(karyotypeName, parentGenome);
      row.addAberrations(kt.getAberrations());
      this.kiT.addRow(row);

      for (Map.Entry<String, Aberration> ktrid: row.getKaryotypeTableRowIds().entrySet())
        {
        KaryotypeRow krow = new KaryotypeRow(ktrid.getKey());
        krow.addKaryotype(karyotypeName);
        krow.addAberration(ktrid.getValue());

        log.info(krow.getRowIdAsString());
        this.kT.addRow(krow);
        }

      //kt.getAneuploidies()
      // TODO I need a separate table for aneuploidies!!!

      }

    }


  public List<HBaseKaryotype> getKaryotypes() throws IOException
    {
    KaryotypeIndexTable.KaryotypeIndexResult kiResults = this.kiT.queryTable(this.rowId);
    //kiResults.

    //List<KaryotypeResult> = this.kT.getRows();
    return null;
    }

  public List<HBaseChromosome> getChromosomes() throws IOException
    {
    List<HBaseChromosome> chromosomes = new ArrayList<HBaseChromosome>();
    for (String chr: this.getGenome().getChromosomes())
      chromosomes.add( getChromosome(chr) );
    return chromosomes;
    }


  public GenomeResult getGenome()
    {
    return this.genome;
    }

  @Override
  protected void getTables() throws IOException
    {
    this.gT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getGenomeTable();
    this.cT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getChromosomeTable();
    this.kT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getKaryotypeTable();
    this.kiT = HBaseGenomeAdmin.getHBaseGenomeAdmin().getKaryotypeIndexTable();
    }
  }
