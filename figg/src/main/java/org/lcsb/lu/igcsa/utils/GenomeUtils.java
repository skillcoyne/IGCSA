package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenomeUtils
  {
  static Logger log = Logger.getLogger(GenomeUtils.class.getName());

  public static List<Chromosome> getChromosomesFromFASTA(File fastaDir) throws FileNotFoundException, ProbabilityException, InstantiationException, IllegalAccessException
    {
    if (!fastaDir.exists() || !fastaDir.canRead())
      throw new FileNotFoundException("FASTA directory does not exist or is not readable " + fastaDir.getAbsolutePath());

    ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
    for (File file : FileUtils.listFASTAFiles(fastaDir, FileUtils.FASTA_FILE))
      {
      String name = FileUtils.getChromosomeFromFASTA(file.getName());
      chromosomes.add(new Chromosome(name, file));
      }

    return chromosomes;
    }

  }
