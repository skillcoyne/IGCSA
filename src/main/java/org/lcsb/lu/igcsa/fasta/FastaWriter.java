package org.lcsb.lu.igcsa.fasta;

import java.io.*;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAWriter
  {
  // 71 seems to be the usual, but I like round numbers and there's no standard for fasta
  private static final int lineLength = 70;

  private File fasta;
  private FileWriter fileWriter;
  private BufferedWriter bufferedWriter;

  private FASTAHeader header;

  public FASTAWriter(File fasta, FASTAHeader header) throws IOException
    {
    this.fasta = createFile(fasta);
    this.header = header;
    writeLine(">" + header.getAccession() + "|" + header.getLocus() + "|" + header.getDescription());
    }

  public void writeLine(String str) throws IOException
    {
    if (str.length() > lineLength)
      {
      int endIndex = lineLength;
      for (int i = 0; i <= str.length(); i += lineLength)
        {
        write(str.substring(i, endIndex) + "\n");
        endIndex += lineLength;
        if (endIndex > str.length()-1) endIndex = str.length();
        }
      }
    else
      { write(str + "\n"); }
    }

  public void write(String str) throws IOException
    {
    bufferedWriter.write(str);
    bufferedWriter.flush();
    }

  public void close() throws IOException
    {
    bufferedWriter.close();
    fileWriter.close();
    }

  public File getFASTAFile()
    {
    return fasta;
    }

  private File createFile(File file) throws IOException
    {
    File parentDir = new File(file.getParent());
    if (!file.exists())
      {
      parentDir.mkdirs();
      try
        { file.createNewFile(); }
      catch (IOException ioe) { throw new IOException(ioe.getMessage() + " " + file.getAbsolutePath()); }
      }
    fileWriter = new FileWriter(file.getAbsoluteFile());
    bufferedWriter = new BufferedWriter(fileWriter);
    return file;
    }

  }
