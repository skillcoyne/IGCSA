package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.Aberration;
import org.lcsb.lu.igcsa.aberrations.Translocation;
import org.lcsb.lu.igcsa.database.BreakpointDAO;
import org.lcsb.lu.igcsa.database.ChromosomeBandDAO;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
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

  private List<DerivativeChromosome> derivativeChromosomes = new ArrayList<DerivativeChromosome>();

  //private List<Aberration> aberrationList = new ArrayList<Aberration>();
  //private Map<String, Aberration> aberrationMap = new LinkedHashMap<String, Aberration>();

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
      addChromosome(chr);
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
    log.info("GAIN chromosome " + chromosome);
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
    if (allosomes.equals("XX")) removeChromosome("Y");
    }

  /**
   * Use for aneuploidy
   *
   * @param name
   */
  public void chromosomeLoss(String name)
    {
    log.info("LOSE chromosome " + name);
    chromosomeCount.put(name, chromosomeCount.get(name) - 1);
    if (chromosomeCount.get(name) == 0) this.removeChromosome(name);
    }

  // TODO this is temporary, when I know how I want to get these from the real data this will change but it belongs in the karyotype class
  public void setAberrations(Properties ktProperties) throws Exception, InstantiationException, IllegalAccessException
    {
    Map<String, List<Aberration>> aberrationMap = KaryotypePropertiesUtil.getAberrationList(bandDAO, ktProperties);
    for (Map.Entry<String, List<Aberration>> entry : aberrationMap.entrySet())
      {
      Chromosome chr = this.getChromosome(entry.getKey());
      DerivativeChromosome derChr = new DerivativeChromosome(chr.getName(), chr);
      derChr.setAberrationList(entry.getValue());

      this.addDerivativeChromosome(derChr);
      }

    Object[] translocations = KaryotypePropertiesUtil.getTranslocations(bandDAO, ktProperties, this.chromosomes);
    if (translocations != null)
      {
      Set<String> chrs = (Set<String>) translocations[0];
      DerivativeChromosome derChr = new DerivativeChromosome(chrs.iterator().next());
      for (String chrName: chrs)
        derChr.addChromosome(this.getChromosome(chrName));
      derChr.setAberrationList((List<Aberration>) translocations[1]);

      this.addDerivativeChromosome(derChr);
      }
    }

  public void addDerivativeChromosome(DerivativeChromosome chr)
    {
    this.derivativeChromosomes.add(chr);
    if (this.ploidyCount(chr.getName()) > 1)
      chromosomeLoss(chr.getName());
    }

  public DerivativeChromosome[] getDerivativeChromosomes()
    {
    return this.derivativeChromosomes.toArray(new DerivativeChromosome[this.derivativeChromosomes.size()]);
    }


  /*
  TODO: Note that the FASTAReader in each chromosome is NOT thread-safe. There is only a single reader pointer for each chromosome so at the moment this isn't multi-threaded. This wouldn't necessarily be difficult to implement. But I'm not sure if it's a simple case of returning a new reader each time getREADER is called on a chromosome since I would then have to be very careful to be aware of when I called that method.  It's also possible that disk I/O could cause these to all slow down and currently sequentially mutating chromosomes is fast enough.
    */
  public StructuralMutable applyAberrations() throws IOException
    {
    for (DerivativeChromosome dchr : this.derivativeChromosomes)
      {
      log.info(dchr.getName());
      for (Aberration abr : dchr.getAberrationList())
        {
        if (abr.getClass().equals(Translocation.class))
          {
          log.info("TRANSLOCATION: " + dchr.getName());
          Translocation trans = (Translocation) abr;

          File newFasta = ktChromosomeFile(this.getGenomeDirectory(), dchr.getName());

          log.info(newFasta.getAbsolutePath());

          FASTAHeader header = new FASTAHeader("figg", newFasta.getName().replaceAll("-der.fa", ""), "karyotype.variation",
                                               this.getBuildName());
          FASTAWriter writer = new FASTAWriter(newFasta, header);
          MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, newFasta.getName().replace(".fa", "")
                                                                                         + "-SVs.txt"), MutationWriter.SV);

          trans.applyAberrations(dchr, writer, mutWriter);
          }
        else
          {
          log.info(abr.getClass());
          // 1. Get chromosome for each fragment
          // 2. Create new fasta file IF APPLICABLE (derivatives or translocations)
          // 3. Give Mutable impl the new fasta file, with the aberration
          // 4. Call Aberration.apply

          File newFasta = ktChromosomeFile(this.getGenomeDirectory(), dchr.getName());

          log.info(newFasta.getAbsolutePath());

          FASTAHeader header = new FASTAHeader("figg", newFasta.getName().replaceAll("-der.fa", ""), "karyotype.variation",
                                               this.getBuildName());
          FASTAWriter writer = new FASTAWriter(newFasta, header);
          MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, newFasta.getName().replace(".fa", "")
                                                                                         + "-SVs.txt"), MutationWriter.SV);
          abr.applyAberrations(dchr, writer, mutWriter);
          }
        }
      }

    return null;
    }

  //  need to check filenames now that I could be creating multiple copies of chromosomes
  private File ktChromosomeFile(File dir, String chr)
    {
    File file = new File(dir, chr + "-der.fa");
    int n = 1;
    while (file.exists())
      {
      file = new File(dir, "chr" + chr + "." + n + "-der.fa");
      ++n;
      }
    return file;
    }

  }
