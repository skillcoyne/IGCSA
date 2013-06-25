package org.lcsb.lu.igcsa.genome;

import org.apache.derby.iapi.services.io.FileUtil;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.Aberration;
import org.lcsb.lu.igcsa.aberrations.Translocation;
import org.lcsb.lu.igcsa.database.BreakpointDAO;
import org.lcsb.lu.igcsa.database.ChromosomeBandDAO;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.concurrency.StructuralMutable;
import org.lcsb.lu.igcsa.utils.KaryotypePropertiesUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Karyotype extends Genome
  {
  static Logger log = Logger.getLogger(Karyotype.class.getName());

  private int ploidy = 46; //default is normal
  private String allosomes;

  // Since in the karyotype it makes a difference how many copies of each chromosome are around we have to track it somehow
  private HashMap<String, Integer> chromosomeCount = new HashMap<String, Integer>();

  private ChromosomeBandDAO bandDAO;
  private BreakpointDAO breakpointDAO;

  //private List<Aberration> aberrationList = new ArrayList<Aberration>();
  private Map<String, Aberration> aberrationMap = new LinkedHashMap<String, Aberration>();

  //44,X,-Y,-6,t(11;14)(q13;q32),add(22)(q13)
  public Karyotype(BreakpointDAO bpDAO, ChromosomeBandDAO bandDAO)
    {
    this.bandDAO = bandDAO;
    this.breakpointDAO = bpDAO;
    }

  public void setKaryotypeDefinition(int ploidy, String allosomes)
    {
    this.ploidy = ploidy;
    this.allosomes = allosomes;
    }

  public int getPloidy()
    {
    return ploidy;
    }

  public String getAllosomes()
    {
    return allosomes;
    }

  public int ploidyCount(String chrName)
    {
    return this.chromosomeCount.get(chrName);
    }

  @Override
  public void addChromosomes(Chromosome[] chromosomes)
    {
    super.addChromosomes(chromosomes);
    for (Chromosome chr : chromosomes)
      chromosomeCount.put(chr.getName(), 2);
    adjustSex();
    }

  /**
   * Do NOT use for aneuploidy
   *
   * @param chromosome
   */
  @Override
  public void addChromosome(Chromosome chromosome)
    {
    super.addChromosome(chromosome);

    chromosomeCount.put(chromosome.getName(), 2);

    adjustSex();
    }

  public void chromosomeGain(String chromosome)
    {
    chromosomeCount.put(chromosome, chromosomeCount.get(chromosome) + 1);
    }

  private void adjustSex()
    {
    // Make sure males start with only one X and one Y
    if (allosomes.equals("XY"))
      {
      chromosomeCount.put("X", 1);
      chromosomeCount.put("Y", 1);
      }

    // females don't need to have Y chromosome around
    if (allosomes.equals("XX")) loseChromosome("Y");
    }

  /**
   * Use for aneuploidy
   *
   * @param name
   */
  public void chromosomeLoss(String name)
    {
    chromosomeCount.put(name, chromosomeCount.get(name) - 1);
    if (chromosomeCount.get(name) == 0) this.loseChromosome(name);
    }

  // TODO this is temporary, when I know how I want to get these from the real data this will change but it belongs in the karyotype class
  public void setAberrations(Properties ktProperties) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
    try
      {
      List<Aberration> aberrationList = KaryotypePropertiesUtil.getAberrationList(bandDAO, ktProperties);
      for (Aberration a : aberrationList)
        this.aberrationMap.put(a.getClass().getSimpleName(), a);
      }
    catch (Exception e)
      {
      log.error(e);
      }
    }

  public void addAbberation(Aberration abr)
    { // e.g. Translocations should come after deletions, indels etc.
    this.aberrationMap.put(abr.getClass().getSimpleName(), abr);
    }

  public Collection<Aberration> getAberrations()
    {
    return this.aberrationMap.values();
    }

  public Aberration getAberrationByType(String name)
    {
    return this.aberrationMap.get(name);
    }

  public StructuralMutable applyAberrations() throws IOException
    {
    for (Aberration abr : this.aberrationMap.values())
      {
      if (abr.getClass().equals(Translocation.class))
        {
        Translocation trans = (Translocation) abr;
        // TODO this might not be necessary in future, but currently the chromosome objects are lacking fasta files/readers
        String derChrName = "der-";
        Iterator<Map.Entry<ChromosomeFragment, Chromosome>> tI = trans.getFragments().entrySet().iterator();
        while (tI.hasNext())
          {
          Map.Entry<ChromosomeFragment, Chromosome> entry = tI.next();
          trans.addFragment(entry.getKey(), this.getChromosome(entry.getValue().getName()));
          derChrName = derChrName + "." + entry.getValue().getName();
          }

        // translocations are a bit different in that the chromosome provided to "apply" isn't used
        FASTAHeader header = new FASTAHeader("figg", derChrName, "karyotype.variation", this.getBuildName());
        // probably need to check filenames now that I could be creating multiple copies of chromosomes
        FASTAWriter writer = new FASTAWriter(new File(this.getGenomeDirectory(), derChrName + "-kt.fa"), header);
        MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, derChrName + "-SVs.txt"), MutationWriter.SMALL);

        trans.applyAberrations(null, writer, mutWriter);
        }
      else
        {
        // 1. Get chromosome for each fragment
        // 2. Create new fasta file IF APPLICABLE (derivatives or translocations)
        // 3. Give Mutable impl the new fasta file, with the aberration
        // 4. Call Aberration.apply
        for (Map.Entry<String, TreeSet<Location>> entry : abr.getFragmentLocations().entrySet())
          {
          Chromosome chr = this.getChromosome(entry.getKey());

          File newFasta = ktChromosomeFile(this.getGenomeDirectory(), chr.getName());
          FASTAHeader header = new FASTAHeader("figg", newFasta.getName().replaceAll("-kt.fa", ""), "karyotype.variation",
                                               this.getBuildName());
          FASTAWriter writer = new FASTAWriter(newFasta, header);
          MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, chr.getName() + "-SVs.txt"), MutationWriter.SMALL);

          abr.applyAberrations(chr, writer, mutWriter);
          }
        }
      }

    return null;
    }

  //  need to check filenames now that I could be creating multiple copies of chromosomes
  private File ktChromosomeFile(File dir, String chr)
    {
    File file = new File(dir, "chr" + chr + "-kt.fa");
    int n = 1;
    while (file.exists())
      {
      file = new File(dir, "chr" + chr + "." + n + "-kt.fa");
      ++n;
      }
    return file;
    }

  }
