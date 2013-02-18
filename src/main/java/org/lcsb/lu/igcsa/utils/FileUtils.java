package org.lcsb.lu.igcsa.utils;

import org.lcsb.lu.igcsa.genome.Chromosome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Set;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FileUtils
  {

  public static String getChromosomeFromFASTA(File file)
    {
    String fastaChr = file.getName().replace("chr", "");
    fastaChr = fastaChr.replaceAll(fastaChr.substring(fastaChr.indexOf("."), fastaChr.length()  ), "");
    return fastaChr;
    }

  public static Chromosome[] getChromosomesFromFASTA(File fastaDir) throws FileNotFoundException
    {
    if (!fastaDir.exists()) throw new FileNotFoundException("No such directory: " + fastaDir.getAbsolutePath());
    ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
      for (File file : listFASTAFiles(fastaDir))
        {
        String chr = getChromosomeFromFASTA(file);
        chromosomes.add(new Chromosome(chr, file));
        }
    return chromosomes.toArray(new Chromosome[chromosomes.size()]);
    }

  public static File[] listFASTAFiles(File fastaDir) throws FileNotFoundException
    {
    FilenameFilter fastaFilter = new FilenameFilter()
      {
      public boolean accept(File dir, String name)
        {
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.endsWith(".fa") || lowercaseName.endsWith(".fasta") || (lowercaseName.endsWith(".fa.gz"))) return true;
        else return false;
        }
      };
    return fastaDir.listFiles(fastaFilter);
    }

  }
