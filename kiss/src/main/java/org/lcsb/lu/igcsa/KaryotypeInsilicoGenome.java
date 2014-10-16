//package org.lcsb.lu.igcsa;
//
//import org.apache.commons.cli.CommandLine;
//import org.apache.log4j.Logger;
//import org.lcsb.lu.igcsa.fasta.Mutation;
//import org.lcsb.lu.igcsa.fasta.MutationWriter;
//import org.lcsb.lu.igcsa.generator.KaryotypeGenerator;
//import org.lcsb.lu.igcsa.genome.*;
//import org.lcsb.lu.igcsa.prob.ProbabilityException;
//import org.lcsb.lu.igcsa.population.utils.FileUtils;
//import org.lcsb.lu.igcsa.population.utils.KaryotypeWriter;
//import org.springframework.context.ApplicationContext;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.Properties;
//import static org.lcsb.lu.igcsa.population.utils.GenomeUtils.*;
//
///**
// * org.lcsb.lu.igcsa
// * Author: sarah.killcoyne
// * Copyright Luxembourg Centre for Systems Biomedicine 2013
// * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
// */
//public class KaryotypeInsilicoGenome
//  {
//  static Logger log = Logger.getLogger(KaryotypeInsilicoGenome.class.getName());
//
//  // Spring
//  private ApplicationContext context;
//
//  protected Properties genomeProperties;
//  private Properties karyotypeProperties;
//  protected Karyotype genome;
//  private KaryotypeGenerator karyotypeGenerator;
//
//  private int count = 2;
//
//
//  public KaryotypeInsilicoGenome(ApplicationContext context, CommandLine cl) throws Exception
//    {
//    this.context = context;
//    if (cl != null)
//      {
//      init(cl.getOptionValue('n'));
//      count = Integer.parseInt(cl.getOptionValue('s', "2"));
//      }
//    else
//      {
//      init("Test"); // TODO CHANGE THIS
//      count = 1;
//      }
//
//    }
//
//  public Karyotype getGenome()
//    {
//    return genome;
//    }
//
//  public void applyMutations()
//    {
//    log.info("Apply structural variations to " + genome.getBuildName() + ". " + count + " karyotypes will be generated.");
//    for (int i = 1; i <= count; i++)
//      {
//      try
//        {
//        Karyotype karyotype = genome.copy();
//
//        karyotype.setBuildName(karyotype.getBuildName() + "-karyotype-" + i);
//
//        log.info(karyotype.getBuildName() + " structural variations");
//
//
//        File ktDir = new File(genome.getGenomeDirectory(), i + "-SV");
//        if (ktDir.exists())
//          throw new IOException("Cannot create " + ktDir.getAbsolutePath() + " path already exists.");
//        else
//          ktDir.mkdirs();
//
//        karyotype.setGenomeDirectory(ktDir);
//        File svWriterPath = new File(karyotype.getGenomeDirectory(), "structural-variations");
//        if (!svWriterPath.exists() || !svWriterPath.isDirectory())
//          svWriterPath.mkdir();
//
//        karyotype.setMutationDirectory(svWriterPath);
//        karyotype = karyotypeGenerator.generateKaryotypes(karyotype);
//        //karyotype.applyAberrations();
//
//        MutationWriter ploidyWriter = new MutationWriter(new File(svWriterPath, "normal-ploidy.txt"), MutationWriter.PLOIDY);
//        //        log.info("Copying normal chromosome files.");
//        for (Chromosome c : karyotype.getChromosomes())
//          {
//          //          if (karyotype.ploidyCount(c.getName()) > 0)
//          //            {
//          //            log.debug("Copying chromosme file " + c.getFASTA().getAbsolutePath() + " to " + karyotype.getGenomeDirectory());
//          //            org.apache.commons.io.FileUtils.copyFile(c.getFASTA(), new File(karyotype.getGenomeDirectory(), c.getFASTA().getName()));
//          //            }
//          ploidyWriter.write(new Mutation(c.getName(), karyotype.ploidyCount(c.getName())));
//          }
//        ploidyWriter.close();
//
//        KaryotypeWriter ktWriter = new KaryotypeWriter(karyotype, new File(ktDir, "karyotype-desc.txt"));
//        ktWriter.write();
//        }
//      catch (ProbabilityException e)
//        {
//        log.error(e);
//        }
//      catch (IOException e)
//        {
//        log.error(e);
//        }
//      }
//    log.info("*** Finished structural variation. ***");
//    }
//
//  // get all beans
//  private void init(String name) throws Exception
//    {
//    genomeProperties = (Properties) context.getBean("genomeProperties");
//    karyotypeProperties = (Properties) context.getBean("karyotypeProperties");
//
//    //    tables = (Karyotype) context.getBean("karyotype");
//    genome = new Karyotype();
//    genome.setKaryotypeDefinition(Integer.parseInt(karyotypeProperties.getProperty("ploidy")), karyotypeProperties.getProperty("sex"));
//    genome.setBuildName(name);
//
//    // should be reading previously mutated tables
//    File earlierMutations = new File(genomeProperties.getProperty("dir.insilico"), name);
//    // Set up the chromosomes in the tables that will be mutated, these come from the previously mutated tables if there was one
//    File fastaDir = new File(genomeProperties.getProperty("dir.assembly"));
//
//    /*
//   If there are already chromosomes set up in the named tables directory we use those because the means fragment variations were
//   applied first. If not we'll start from the assembly.
//    */
//    if (!earlierMutations.exists() || earlierMutations.listFiles().length <= 0)
//      {
//      List<Chromosome> chrList = getChromosomesFromFASTA(fastaDir);
//      log.info("Copying normal chromosome files.");
//      for (Chromosome c : chrList)
//        {
//        log.debug("Copying chromosme file " + c.getFASTA().getAbsolutePath() + " to " + earlierMutations);
//        org.apache.commons.io.FileUtils.copyFile(c.getFASTA(), new File(earlierMutations, c.getFASTA().getName()));
//        }
//      fastaDir = earlierMutations;
//      }
//
//      genome.setParentGenomePath(fastaDir);
//
//      log.info("Genome directory to read from is: " + fastaDir.getAbsolutePath());
//
//      List<Chromosome> chrList = getChromosomesFromFASTA(fastaDir);
//      genome.addChromosomes(chrList.toArray(new Chromosome[chrList.size()]));
//      karyotypeGenerator = (KaryotypeGenerator) context.getBean("karyotypeGenerator");
//
//      File parent = new File(new File(genomeProperties.getProperty("dir.insilico"), genome.getBuildName()), "karyotypes");
//      genome.setGenomeDirectory(parent);
//      }
//
//    }
