package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

/**
 * BufferedFASTAReader takes care of all of the tracking for the FASTA header, reading the location within a sequence
 * This works ONLY for a FASTA file that contains a single record such as whole chromosome files.  This is primarily to clean
 * the skipping location stuff out of the FASTAReader class.
 */
public class BufferedFASTAReader extends BufferedReader
  {
  static Logger log = Logger.getLogger(BufferedFASTAReader.class.getName());

  private FASTAHeader header;
  private int sequenceStartLocation;
  private int sequenceLineLength;

  private long currentSeqLoc = 0L;

  public BufferedFASTAReader(Reader reader, int i)
    {
    super(reader, i);
    fastaInfo();
    }

  public BufferedFASTAReader(Reader reader)
    {
    super(reader);
    fastaInfo();
    }


  private void fastaInfo()
    {
    try
      {
      String line = this.readLine();
      sequenceStartLocation = line.length();
      header = new FASTAHeader(line);

      mark(sequenceStartLocation);
      sequenceLineLength = this.readLine().length();
      reset();
      currentSeqLoc = sequenceStartLocation;
      }
    catch (IOException e)
      {
      log.error(e);
      }
    }


  public FASTAHeader getHeader() throws IOException
    {
    return header;
    }

  public int getSequenceStartLocation()
    {
    return sequenceStartLocation;
    }

  public int getSequenceLineLength()
    {
    return sequenceLineLength;
    }

  public long getCurrentSequenceLocation()
    {
    return currentSeqLoc;
    }


  @Override
  public int read() throws IOException
    {
    ++currentSeqLoc;
    return super.read();
    }

  /**
   * This skips to the given sequence character.  It ignores the header of the file.
   * @param l
   * @return
   * @throws IOException
   */
  @Override
  public long skip(long l) throws IOException
    {
    if (l == currentSeqLoc) return currentSeqLoc;

    /*
    Since every line has a line feed character, the start location needs to be incremented
    by the number of lines that have to be skipped or else the characters read in are off by 1 (or more)
    */
    if (l > sequenceLineLength)
      l += Math.round(l/sequenceLineLength);


    l += this.sequenceStartLocation-1;


    if (currentSeqLoc > l)
      throw new IOException("Reader is ahead of skip location, reset reader to access this.");
    else
      l = l - currentSeqLoc;

    currentSeqLoc = l;
    return super.skip(l);
    }
  }
