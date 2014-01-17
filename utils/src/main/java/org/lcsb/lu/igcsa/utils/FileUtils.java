package org.lcsb.lu.igcsa.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FileUtils
  {
  private static final Log log = LogFactory.getLog(FileUtils.class);

  public static final FilenameFilter FASTA_FILE = new FilenameFilter()
  {
  @Override
  public boolean accept(File dir, String name)
    {
    for (String suffix : new String[]{".fa", ".fasta", ".fa.gz", ".fasta.gz"})
      if (name.endsWith(suffix)) return true;
    return false;
    }
  };


  public static File getFASTA(String chromosome, File fastaDir) throws IOException
    {
    final String chr = "chr" + chromosome;
    FilenameFilter fastaFilter = new FilenameFilter()
    {
    public boolean accept(File dir, String name)
      {
      for (String suffix : new String[]{".fa", ".fasta", ".fa.gz", ".fasta.gz"})
        if (name.equalsIgnoreCase(chr + suffix)) return true;
      return false;
      }
    };

    File[] files = listFASTAFiles(fastaDir, fastaFilter);
    if (files.length > 1) throw new IOException("Multiple FASTA files identified for chromosome " + chromosome);
    if (files.length <= 0)
      throw new IOException("No FASTA file found for chromosome " + chromosome + " in directory " + fastaDir.getAbsolutePath());

    return files[0];
    }


  /**
   * @param fileName
   * @return Chromosome object with the name set based on the file name.
   */
  public static String getChromosomeFromFASTA(String fileName)
    {
    Pattern p = Pattern.compile("(\\d{1,2}|X|Y)\\.fa.*$");
    Matcher matcher = p.matcher(fileName);

    if (matcher.matches())
      {
      log.info("Chromosome from FASTA " + fileName + ": " + matcher.group(1));
      return matcher.group(1);
      }
    return null;
    }

  public static Map<String, File> getFASTAFiles(File fastaDir) throws FileNotFoundException
    {
    if (!fastaDir.exists() || !fastaDir.canRead())
      throw new FileNotFoundException("FASTA directory does not exist or is not readable " + fastaDir.getAbsolutePath());

    Map<String, File> files = new HashMap<String, File>();
    for (File file : listFASTAFiles(fastaDir, FASTA_FILE))
      {
      String name = getChromosomeFromFASTA(file.getName());
      files.put(name, file);
      }
    return files;
    }

  public static File[] listFASTAFiles(File fastaDir, FilenameFilter filter) throws FileNotFoundException
    {
    File[] fastaFiles = fastaDir.listFiles(filter);
    if (fastaFiles.length <= 0)
      throw new FileNotFoundException("No FASTA files found in directory (.fa, .fasta, .fa.gz, .fasta.gz) " + fastaDir);
    return fastaFiles;
    }


  public static void ZipDirectory(File srcDir, String zipFile) throws IOException
    {
    FileOutputStream fos = new FileOutputStream(zipFile);
    ZipOutputStream zos = new ZipOutputStream(fos);
    addDirToArchive(zos, srcDir);
    zos.close();
    }

  private static void addDirToArchive(ZipOutputStream zos, File srcFile) throws IOException
    {
    File[] files = srcFile.listFiles();

    for (int i = 0; i < files.length; i++)
      {
      // if the file is directory, use recursion
      if (files[i].isDirectory())
        {
        addDirToArchive(zos, files[i]);
        continue;
        }

      // create byte buffer
      byte[] buffer = new byte[1024];
      FileInputStream fis = new FileInputStream(files[i]);
      zos.putNextEntry(new ZipEntry(files[i].getName()));

      int length;
      while ((length = fis.read(buffer)) > 0) zos.write(buffer, 0, length);

      zos.closeEntry();
      fis.close();
      }
    }


  }
