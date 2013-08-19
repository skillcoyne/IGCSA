package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.SequenceAberration;
import org.lcsb.lu.igcsa.aberrations.multiple.DerivativeChromosomeAberration;
import org.lcsb.lu.igcsa.aberrations.multiple.Translocation;
import org.lcsb.lu.igcsa.aberrations.single.SingleChromosomeAberration;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.genome.concurrency.Mutable;

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
  private List<DerivativeChromosome> derivativeChromosomes = new ArrayList<DerivativeChromosome>();
  private Map<Object, List<Collection<Band>>> aberrations = new HashMap<Object, List<Collection<Band>>>();
  private List<Aberration> aberrationDefinitions = new ArrayList<Aberration>();

  //44,X,-Y,-6,t(11;14)(q13;q32),add(22)(q13)

  public void setKaryotypeDefinition(int ploidy, String allosomes)
    {
    this.ploidy = ploidy;
    this.allosomes = allosomes;
    adjustSex();
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

  public HashMap<String, Integer> getChromosomeCount()
    {
    return chromosomeCount;
    }

  public int getChromosomeCount(String chr)
    {
    return chromosomeCount.get(chr);
    }

  @Override
  public void addChromosomes(Chromosome[] chromosomes)
    {
    super.addChromosomes(chromosomes);
    for (Chromosome chr : chromosomes)
      addChromosome(chr);
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

    if (!chromosome.getName().matches("X|Y"))
      chromosomeCount.put(chromosome.getName(), 2);
    }

  /**
   * Use for aneuploidy
   *
   * @param chromosome
   */
  public void gainChromosome(String chromosome)
    {
    log.info("GAIN chromosome " + chromosome);
    chromosomeCount.put(chromosome, chromosomeCount.get(chromosome) + 1);
    }

  /**
   * Use for aneuploidy
   *
   * @param name
   */
  public void loseChromosome(String name)
    {
    log.info("LOSE chromosome " + name);
    chromosomeCount.put(name, chromosomeCount.get(name) - 1);
//    if (chromosomeCount.get(name) == 0)
//      this.removeChromosome(name);
    }

  public Map<Object, List<Collection<Band>>> getAberrations()
    {
    return aberrations;
    }

  public void addAberrationDefintion(Aberration abr)
    {
    this.aberrationDefinitions.add(abr);

    // Derivative chromosome will be dependent on what the first chromosome in any given aberration is
    String chromosomeName = abr.getBands().get(0).getChromosomeName();
    DerivativeChromosome dChr = new DerivativeChromosome(chromosomeName, this.getChromosome(chromosomeName));

    SequenceAberration sequenceAberration;

    try
      {
      if (SequenceAberration.hasClassForAberrationType((String) abr.getAberration()))
        {
        sequenceAberration = (SequenceAberration) SequenceAberration.getClassForAberrationType((String) abr.getAberration()).newInstance();
        for (Band b : abr.getBands())
          sequenceAberration.addFragment(b, this.getChromosome(b.getChromosomeName()));

        dChr.addSequenceAberration(sequenceAberration);
        }
      else log.warn("No class for aberration type '" + abr.getAberration() + "'");
      }
    catch (InstantiationException e)
      {
      log.error(e);
      }
    catch (IllegalAccessException e)
      {
      log.error(e);
      }

    this.addDerivativeChromosome(dChr);
    }

  public List<Aberration> getAberrationDefinitions()
    {
    return aberrationDefinitions;
    }

  public void addDerivativeChromosome(DerivativeChromosome chr)
    {
    this.derivativeChromosomes.add(chr);
    if (this.ploidyCount(chr.getName()) > 1) loseChromosome(chr.getName());
    }

  public DerivativeChromosome[] getDerivativeChromosomes()
    {
    return this.derivativeChromosomes.toArray(new DerivativeChromosome[this.derivativeChromosomes.size()]);
    }


  /*
  TODO: Note that the FASTAReader in each chromosome is NOT thread-safe. There is only a single reader pointer for each chromosome so at
  the moment this isn't multi-threaded. This wouldn't necessarily be difficult to implement. But I'm not sure if it's a simple case of
  returning a new reader each time getREADER is called on a chromosome since I would then have to be very careful to be aware of when I
  called that method.  It's also possible that disk I/O could cause these to all slow down and currently sequentially mutating
  chromosomes is fast enough.
    */
  public Mutable applyAberrations() throws IOException
    {
    for (DerivativeChromosome dchr : this.derivativeChromosomes)
      {
      log.debug(dchr.getName());
      for (SequenceAberration abr : dchr.getSequenceAberrationList())
        {
        log.debug(abr.getClass().getSimpleName() + " " + dchr.getName());
        File newFasta = ktChromosomeFile(this.getGenomeDirectory(), dchr.getName());

        log.debug(newFasta.getAbsolutePath());

        FASTAHeader header = new FASTAHeader("figg", newFasta.getName().replaceAll("-der.fa", ""), "karyotype.variation",
                                             this.getBuildName());
        FASTAWriter writer = new FASTAWriter(newFasta, header);
        MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, newFasta.getName().replace(".fa",
                                                                                                                  "") + "-SVs.txt"),
                                                      MutationWriter.SV);
        abr.applyAberrations(dchr, writer, mutWriter);

//        if (abr.getClass().equals(Translocation.class))
//          {
//          log.info("TRANSLOCATION: " + dchr.getName());
//          Translocation trans = (Translocation) abr;
//
//          File newFasta = ktChromosomeFile(this.getGenomeDirectory(), dchr.getName());
//
//          log.info(newFasta.getAbsolutePath());
//
//          FASTAHeader header = new FASTAHeader("figg", newFasta.getName().replaceAll("-der.fa", ""), "karyotype.variation",
//                                               this.getBuildName());
//          FASTAWriter writer = new FASTAWriter(newFasta, header);
//          MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, newFasta.getName().replace(".fa",
//                                                                                                                    "") + "-SVs.txt"),
//                                                        MutationWriter.SV);
//
//          //trans.applyAberrations(dchr, writer, mutWriter);
//          }
//        else
//          {
//          log.info(abr.getClass());
//          // 1. Get chromosome for each fragment
//          // 2. Create new fasta file IF APPLICABLE (derivatives or translocations)
//          // 3. Give Mutable impl the new fasta file, with the aberration
//          // 4. Call SequenceAberration.apply
//
//          File newFasta = ktChromosomeFile(this.getGenomeDirectory(), dchr.getName());
//
//          log.info(newFasta.getAbsolutePath());
//
//          FASTAHeader header = new FASTAHeader("figg", newFasta.getName().replaceAll("-der.fa", ""), "karyotype.variation",
//                                               this.getBuildName());
//          FASTAWriter writer = new FASTAWriter(newFasta, header);
//          MutationWriter mutWriter = new MutationWriter(new File(this.mutationDirectory, newFasta.getName().replace(".fa",
//                                                                                                                    "") + "-SVs.txt"),
//                                                        MutationWriter.SV);
//          //abr.applyAberrations(dchr, writer, mutWriter);
//          }
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

  private void adjustSex()
    {
    // Make sure males start with only one X and one Y
    if (allosomes.equals("XY"))
      {
      chromosomeCount.put("X", 1);
      chromosomeCount.put("Y", 1);
      }

    // females don't need to have Y chromosome around
    if (allosomes.equals("XX"))
      {
      removeChromosome("Y");
      chromosomeCount.put("X", 2);
      }
    }


  @Override
  public Karyotype copy()
    {
    Karyotype kt = new Karyotype();

    kt.setBuildName(this.buildName);
    kt.setGenomeDirectory(this.getGenomeDirectory());
    for (Chromosome chr : this.getChromosomes())
      kt.addChromosome(chr);
    kt.setMutationDirectory(this.getMutationDirectory());

    kt.ploidy = this.ploidy;
    kt.chromosomes = this.chromosomes;
    kt.allosomes = this.allosomes;
    kt.chromosomeCount = this.chromosomeCount;

    return kt;
    }


  }
