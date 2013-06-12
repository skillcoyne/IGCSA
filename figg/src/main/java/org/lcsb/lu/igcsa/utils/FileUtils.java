package org.lcsb.lu.igcsa.utils;

import org.lcsb.lu.igcsa.FragmentInsilicoGenome;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FileUtils
  {
  public static File getFASTA(String chromosome, File fastaDir) throws IOException
    {
    final String chr = "chr"+chromosome;
    FilenameFilter fastaFilter = new FilenameFilter()
    {
    public boolean accept(File dir, String name)
      {
      for ( String suffix: new String[]{".fa", ".fasta", ".fa.gz", ".fasta.gz"} )
        if (name.equalsIgnoreCase(chr+suffix)) return true;
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

  public static Chromosome[] getChromosomesFromFASTA(File fastaDir) throws FileNotFoundException, ProbabilityException, InstantiationException, IllegalAccessException
    {
    if (!fastaDir.exists())
      throw new FileNotFoundException("No such directory: " + fastaDir.getAbsolutePath());

    FilenameFilter fastaFilter = new FilenameFilter()
    {
    public boolean accept(File dir, String name)
      {
      for ( String suffix: new String[]{".fa", ".fasta", ".fa.gz", ".fasta.gz"} )
        if (name.endsWith(suffix)) return true;
      return false;
      }
    };

    ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
    for (File file : listFASTAFiles(fastaDir, fastaFilter))
      {
      String name = getChromosomeFromFASTA(file);

      Chromosome chr = new Chromosome(name, file);
      chr.setVariantList(FragmentInsilicoGenome.variantUtils.getVariantList(chr.getName()));
      chromosomes.add(chr);
      }
    return chromosomes.toArray(new Chromosome[chromosomes.size()]);
    }

  private static File[] listFASTAFiles(File fastaDir, FilenameFilter filter) throws FileNotFoundException
    {
    return fastaDir.listFiles(filter);
    }




  }
