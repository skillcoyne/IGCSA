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
    if (files.length > 1) throw new IOException("Multiple FASTA files identified for chromosome " + chromosome);
    if (files.length <= 0) throw new IOException("No FASTA file found for chromosome " + chromosome + " in directory " + fastaDir.getAbsolutePath());

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
    if (!fastaDir.exists() || !fastaDir.canRead())
      throw new FileNotFoundException("FASTA directory does not exist or is not readable " + fastaDir.getAbsolutePath());

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
      // TODO fix this, karytoype doesn't care but small-variations require the list
      //chr.setVariantList(FragmentInsilicoGenome.variantUtils.getVariantList(chr.getName()));
      chromosomes.add(chr);
      }
    return chromosomes.toArray(new Chromosome[chromosomes.size()]);
    }

  private static File[] listFASTAFiles(File fastaDir, FilenameFilter filter) throws FileNotFoundException
    {
    File[] fastaFiles = fastaDir.listFiles(filter);
    if (fastaFiles.length <= 0) throw new FileNotFoundException("No FASTA files found in directory (.fa, .fasta, .fa.gz, .fasta.gz) " + fastaDir);
    return fastaFiles;
    }




  }
