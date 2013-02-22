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
  public static String directory(String s, int number)
    {
    String[] dir = {s};
    return directory(dir, number);
    }

  public static String directory(String[] orderedSubDir, int number)
    {
    String dirName = "";
    for (String s: orderedSubDir)
      {
      if (dirName.equals("")) dirName = s;
      else dirName = dirName + File.separator + s;
      }
    dirName = dirName + "-" + number;
    return dirName;
    }


  /**
   * @param parent
   * @param children
   * @return Collection of child directory File objects
   * @throws IOException
   */
  public static Collection<File> createDirectories(File parent, String[] children) throws IOException
    {
    ArrayList<File> dirs = new ArrayList<File>();
    if (!parent.exists()) parent.mkdirs();

    if (parent.isDirectory())
      {
      for (String child: children)
        {
        File childDir = new File(parent, child);
        if (childDir.exists() || childDir.mkdirs()) dirs.add(childDir);
        }
      }
    else
      {
      throw new IOException("Parent file is not a directory: " + parent.getAbsolutePath());
      }
    if (dirs.size()!=children.length) throw new IOException("Failed to create all child directories");

    return dirs;
    }

  /**
   * @param file
   * @return Chromosome object with the name set based on the file name.
   */
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
