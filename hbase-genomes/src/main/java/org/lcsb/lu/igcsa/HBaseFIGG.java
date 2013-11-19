package org.lcsb.lu.igcsa; /**
 * PACKAGE_NAME
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.FragmentDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.hbase.HBaseMutableSequence;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.HBaseSequence;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.utils.VariantUtils;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.*;

public class HBaseFIGG
  {
  static Logger log = Logger.getLogger(HBaseFIGG.class.getName());

  public static VariantUtils variantUtils;

  protected Properties genomeProperties;

  private int windowSize = 1000; // default


  // Spring
  private ApplicationContext context;

  public HBaseFIGG(ApplicationContext context, CommandLine cl) throws Exception
    {
    this.context = context;
    init();

    String genomeName = String.valueOf(new Random().nextInt(Math.abs((int) (System.currentTimeMillis()))));
    if (cl.hasOption('n'))
      genomeName = cl.getOptionValue('n');

    String parentName = null;
    if (cl.hasOption('p'))
      parentName = cl.getOptionValue('p');

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin();

//    if (admin.getGenome(genomeName) != null)
//      throw new IOException(genomeName + " already exists.");

    // genome does not exist, create
    if (parentName == null)
      createGenomeFromFASTA(genomeName, new File(genomeProperties.getProperty("dir.assembly")));
    else
      {
      // resetting genome for testing
      admin.deleteGenome(genomeName);
      createGenomeFromParent(genomeName, parentName);
      }

    }


  private HBaseGenome createGenomeFromFASTA(String genomeName, File fastaDir) throws Exception, ProbabilityException, IllegalAccessException, InstantiationException, InterruptedException, ExecutionException
    {
    HBaseGenome genome = new HBaseGenome(genomeName, null);

    log.info("Reading FASTA directory " + fastaDir.getAbsolutePath());
    if (!fastaDir.exists() || !fastaDir.canRead())
      throw new IOException("FASTA directory does not exist or is not readable: " + fastaDir.getAbsolutePath());

    ExecutorService chromosomeService = Executors.newCachedThreadPool();
    CompletionService taskPool = new ExecutorCompletionService(chromosomeService);
    List<Future<HBaseChromosome>> tasks = new ArrayList<Future<HBaseChromosome>>();

    List<Chromosome> chromosomeList = FileUtils.getChromosomesFromFASTA(fastaDir);
    for (Chromosome c : chromosomeList)
      {
      FragmentChromosome fragment = new FragmentChromosome(genome.addChromosome(c.getName(), 0, 0), c.getFASTAReader(), genome);
      log.info("Chromsome " + c.getName() + " start");
      fragment.call();
      log.info("Chromsome " + c.getName() + " finish");

      // Fails when I try to thread it.  Probably an issue with sync in Hbase?
 //      Future<HBaseChromosome> ctask = taskPool.submit( );
//      tasks.add(ctask);
      }

//    for (int i = 0; i < tasks.size(); i++)
//      {
//      Future<HBaseChromosome> f = taskPool.take();
//      HBaseChromosome chromosome = f.get();
//
//      log.info("Finished loading " + chromosome.getChromosome().getChrName());
//      }
//    chromosomeService.shutdown();

    return genome;
    }

  private HBaseGenome createGenomeFromParent(String genomeName, String parentName) throws Exception, ProbabilityException, InstantiationException, IllegalAccessException, InterruptedException, ExecutionException
    {
    log.info("Creating from " + parentName);
    HBaseGenome parent = HBaseGenomeAdmin.getHBaseGenomeAdmin().getGenome(parentName);
    HBaseGenome child = new HBaseGenome(genomeName, parentName);

    // TODO this should be MR'd so I don't have to worry about threads
    for (HBaseChromosome parentChr : parent.getChromosomes())
      {
      long startTime = System.currentTimeMillis();

      // add to child
      HBaseChromosome childChr = child.addChromosome(parentChr.getChromosome().getChrName(), 0, 0);

      ExecutorService smallMutationExcecutor = Executors.newFixedThreadPool(100);
      CompletionService taskPool = new ExecutorCompletionService<String>(smallMutationExcecutor);
      List<Future<HBaseSequence>> tasks = new ArrayList<Future<HBaseSequence>>();

      for (int i = 1; i <= parentChr.getChromosome().getSegmentNumber(); i++)
        {
        HBaseSequence parentSeq = parentChr.getSequence(i);

        HBaseSequence childSeq = childChr.addSequence(
            parentSeq.getSequence().getStart(),
            parentSeq.getSequence().getEnd(),
            parentSeq.getSequence().getSegment(),
            parentSeq.getSequence().getSequence());

        HBaseMutableSequence mutable = new HBaseMutableSequence(
            (GCBinDAO) context.getBean("GCBinDAO"),
            (FragmentDAO) context.getBean("FragmentDAO"),
            childSeq,
            variantUtils.getVariantList(parentChr.getChromosome().getChrName()));

        //mutable.call();

        Future<HBaseSequence> mutationF = taskPool.submit(mutable);
        tasks.add(mutationF);
        }

      // this works but it'd be faster to throw it out to MR I would presume
      for (int i = 0; i < tasks.size(); i++)
        {
        Future<HBaseSequence> f = taskPool.take();
        HBaseSequence mutatedChildSeq = f.get();
        log.info("mutated " + mutatedChildSeq.getSequence().getRowId());
        }

      long endTime = System.currentTimeMillis() - startTime;
      log.info("**** Small mutation step finished on " + childChr.getChromosome().getChrName() + endTime);

      smallMutationExcecutor.shutdown();
      }

    return child;
    }


  // Variable initialization. Most of it is done in the Spring configuration files.
  private void init() throws Exception
    {
    // be nice to autowire this so I don't have to make calls into Spring but not important for now
    //genome = (MutableGenome) context.getBean("genome");
    variantUtils = (VariantUtils) context.getBean("variantUtils");
    genomeProperties = (Properties) context.getBean("genomeProperties");

    if (genomeProperties.containsKey("window"))
      {
      windowSize = Integer.valueOf(genomeProperties.getProperty("window"));
      }
    else
      {
      throw new Exception("Property 'window' not found.");
      }

    }


  private class FragmentChromosome implements Callable<HBaseChromosome>
    {
    private HBaseGenome genome;
    private HBaseChromosome chromosome;
    private FASTAReader reader;

    private FragmentChromosome(HBaseChromosome chromosome, FASTAReader reader, HBaseGenome genome)
      {
      this.chromosome = chromosome;
      this.genome = genome;
      this.reader = reader;
      }

    @Override
    public HBaseChromosome call() throws Exception
      {
      String name = this.chromosome.getChromosome().getChrName();

      String seq;
      int segments = 0, totalLength = 0, start = 1;
      while ((seq = reader.readSequence(windowSize)) != null)
        {
        totalLength += seq.length();
        ++segments;

        //log.info("Adding sequence: " + start + "-" + (start + seq.length()) + " " + segments + " " + seq.length());
        chromosome.addSequence(start, (start + seq.length()), segments, seq);

        start = start + seq.length();
        }
      genome.updateChromosome(name, totalLength, segments);
      log.info("updating chromosome " + name + " with " + totalLength + " " + segments);

      return chromosome;
      }
    }


  }
