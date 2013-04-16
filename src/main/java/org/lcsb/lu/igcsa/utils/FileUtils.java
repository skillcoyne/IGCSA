package org.lcsb.lu.igcsa.utils;

import org.lcsb.lu.igcsa.InsilicoGenome;
import org.lcsb.lu.igcsa.genome.Chromosome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FileUtils
  {
  public static String directory(String[] orderedSubDir, int number)
    {
    String dirName = "";
    for (String s : orderedSubDir)
      {
      if (dirName.equals(""))
        dirName = s;
      else
        dirName = dirName + File.separator + s;
      }
    dirName = dirName + "-" + number;
    return dirName;
    }


  public static File getFASTA(String chromosome, File fastaDir) throws IOException
    {
    final String chr = "chr"+chromosome;
    FilenameFilter fastaFilter = new FilenameFilter()
    {
    public boolean accept(File dir, String name)
      {
      String lowercaseName = name.toLowerCase();
      if (lowercaseName.endsWith(chr + ".fa") || lowercaseName.endsWith(chr + ".fasta") || (lowercaseName.endsWith(chr + ".fa.gz")))
        return true;
      else
        return false;
      }
    };

    File[] files = listFASTAFiles(fastaDir, fastaFilter);
    if (files.length > 1) throw new IOException("Multiple fasta files identified for chromosome " + chromosome);

    return files[0];
    }


  /**
   * @param file
   * @return Chromosome object with the name set based on the file name.
   */
  public static String getChromosomeFromFASTA(File file)
    {
    String fastaChr = file.getName().replace("chr", "");
    fastaChr = fastaChr.replaceAll(fastaChr.substring(fastaChr.indexOf("."), fastaChr.length()), "");
    return fastaChr;
    }

  public static Chromosome[] getChromosomesFromFASTA(File fastaDir) throws FileNotFoundException
    {
    if (!fastaDir.exists())
      throw new FileNotFoundException("No such directory: " + fastaDir.getAbsolutePath());

    FilenameFilter fastaFilter = new FilenameFilter()
    {
    public boolean accept(File dir, String name)
      {
      String lowercaseName = name.toLowerCase();
      if (lowercaseName.endsWith(".fa") || lowercaseName.endsWith(".fasta") || (lowercaseName.endsWith(".fa.gz")))
        return true;
      else
        return false;
      }
    };

    ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
    for (File file : listFASTAFiles(fastaDir, fastaFilter))
      {
      String chr = getChromosomeFromFASTA(file);
      chromosomes.add(new Chromosome(chr, file));
      }
    return chromosomes.toArray(new Chromosome[chromosomes.size()]);
    }

  private static File[] listFASTAFiles(File fastaDir, FilenameFilter filter) throws FileNotFoundException
    {
    return fastaDir.listFiles(filter);
    }

  }
