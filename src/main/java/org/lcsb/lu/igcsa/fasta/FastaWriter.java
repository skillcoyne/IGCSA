package org.lcsb.lu.igcsa.fasta;

import java.io.*;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FastaWriter
  {
  private File fasta;
  private FileWriter fileWriter;
  private BufferedWriter bufferedWriter;

  private FASTAHeader header;

  public FastaWriter(File fasta, FASTAHeader header) throws IOException
    {
    this.fasta = createFile(fasta);
    this.header = header;
    write(">" + header.getAccession() + "|" + header.getLocus() + "|" + header.getDescription());
    }

  public void write(String str) throws IOException
    {
    bufferedWriter.write(str);
    }

  public void close() throws IOException
    {
    bufferedWriter.close();
    fileWriter.close();
    }

  private File createFile(File file) throws IOException
    {
    if (!file.exists()) file.createNewFile();
    fileWriter = new FileWriter(file.getAbsoluteFile());
    bufferedWriter = new BufferedWriter(fileWriter);
    return file;
    }

  }
