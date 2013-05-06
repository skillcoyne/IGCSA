package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAWriter
  {
  static Logger log = Logger.getLogger(FASTAWriter.class.getName());
  // ASCII line feed (LF), as char
  private static final char LF = 0x001E;
  // 71 seems to be the usual, but I like round numbers and there's no standard for fasta
  private static final int lineLength = 70;

  private File fasta;
  private FileWriter fileWriter;
  private BufferedWriter bufferedWriter;

  private int totalCharacters = 0;
  private int lines = 0;

  private FASTAHeader header;

  private StringBuffer buffer = new StringBuffer();

  public FASTAWriter(File fasta, FASTAHeader header) throws IOException
    {
    this.fasta = createFile(fasta);
    log.debug(fasta.getAbsolutePath());
    this.header = header;
    //writeString(">" + header.getAccession() + "|" + header.getLocus() + "|" + header.getDescription() + "\n");
    bufferedWriter.write(">" + header.getAccession() + "|" + header.getLocus() + "|" + header.getDescription());
    bufferedWriter.flush();
    }

  public int sequenceLengthWritten()
    {
    return this.totalCharacters;
    }

  public void flush() throws IOException
    {
    totalCharacters += buffer.length();
    bufferedWriter.write(buffer.toString());
    bufferedWriter.flush();
    //writeString(buffer.toString() + "\n");
    buffer = new StringBuffer();
    lines = 0;
    }


  public void write(String str) throws IOException
    {

    for (char c : str.toCharArray())
      {
      if ( (buffer.length() % lineLength) == 0 )
        {
        buffer.append('\n');
        lines += 1;
        }
      if (lines == 1000) flush();

      buffer.append(c);
      }
    }


  public void close() throws IOException
    {
    log.info("Total sequence length written " + this.totalCharacters);
    bufferedWriter.flush();
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
