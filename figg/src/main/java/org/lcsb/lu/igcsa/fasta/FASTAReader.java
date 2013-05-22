package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Location;


import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static org.lcsb.lu.igcsa.genome.Nucleotides.*;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAReader
  {
  static Logger log = Logger.getLogger(FASTAReader.class.getName());

  // The standard gamut of line terminators, plus EOF
  private static final char CARRIAGE_RETURN = 0x000A;
  // ASCII carriage return (CR), as char
  private static final char LINE_FEED = 0x000D;
  // ASCII line feed (LF), as char
  private static final char RECORD_SEPARATOR = 0x001E;
  // ASCII record separator (RS), as char
  private static final char EOF = 0xffff;
  // Reserved characters within the (ASCII) stream
  private final char COMMENT_IDENTIFIER = ';';
  private final char HEADER_IDENTIFIER = '>';


  private long fileloc = 0L;
  // this is actually the end of the header
  private long headerloc = 0L;
  private int seqLineLength;

  private File fasta;
  private InputStream stream;
  private FASTAHeader header;

  private BufferedReader reader;


  public FASTAReader(File file) throws IOException
    {
    fasta = file;
    if (!fasta.exists())
      throw new FileNotFoundException("No such file: " + file.getAbsolutePath());
    if (!fasta.canRead())
      throw new IOException("Cannot read file " + file.getAbsolutePath());

    stream = open();
    //reader = new BufferedReader(new InputStreamReader(this.stream));
    getHeader();
    this.seqLineLength = this.readline().length();
    this.reset();
    }

  private InputStream open() throws IOException
    {
    this.fileloc = 0L;
    //log.debug(fasta.getAbsolutePath() + " file location " + fileloc);

    InputStream is = new FileInputStream(this.fasta);
    if (this.fasta.getName().endsWith("gz"))
      {
      is = new GZIPInputStream(is);
      }
    return is;
    }

  public FASTAHeader getHeader() throws IOException
    {
    if (header == null)
      {
      // there may not always be a header, though with chromosome files there really should be
      header = new FASTAHeader(this.readline());
      this.headerloc = this.fileloc;
      }
    return header;
    }

  public void reset() throws IOException
    {
    stream.close();
    stream = open();
    }


//  public String readSequenceAtLocation(int start, int end) throws IOException
//    {
//    return readSequenceLength(start, end - start);
//    }

  private void skip(long loc) throws IOException
    {
    while (loc > fileloc)
      {
      log.info("skipping " + this.fileloc);
      this.read();
      }
    }

  /**
   * This method creates a new BufferedReader with each call so theoretically it should be capable of being
   * called on the same FASTAReader object
   *
   * @param start       - Offset to start reading file from. The header offset will be added to this so the header line is always skipped.

   * @return String containing the characters read. If the end of the file is found before the total number of characters is reached
   *         the characters read to that point will be returned.
   * @throws IOException
   */
  public String readSequenceFromLocation(int start, int window) throws IOException
    {
    log.info("file location "+ fileloc);
    /*
    Since every line has a line feed character, the start location needs to be incremented
    by the number of lines that have to be skipped or else the characters read in are off by 1 (or more)
    */
    if (start > seqLineLength)
      start += Math.floor(start / seqLineLength);

    start += headerloc;

    if (fileloc > start) this.reset();

    this.skip(start-1);

    return this.readSequence(window) ;
    }

  /**
   * Read and return chunks from the file in order. Each call will advance the read.
   *
   * @param window
   * @return
   * @throws IOException
   */
  public String readSequence(int window) throws IOException
    {
    StringBuffer buf = new StringBuffer();
    char c;
    while ((c = this.read()) != EOF)
      {
      // header has already been read
      if (c == HEADER_IDENTIFIER)
        {
        if (this.header == null)
          getHeader();
        else
          this.skipline();
        continue;
        }
      // don't want the line separators
      if (c == CARRIAGE_RETURN || c == LINE_FEED)
        continue;
      // shouldn't be an issue but it would mean we're done reading
      if (c == RECORD_SEPARATOR)
        break;
      buf.append(Character.toString(c));
      if (buf.length() == window)
        break;
      }

    return buf.toString();
    }


  public int streamToWriter(int start, int end, FASTAWriter writer) throws IOException
    {
    int charWindow = 1000;
    int totalChars = end - start;
    int count = 0;
    if (charWindow > totalChars) charWindow = totalChars;

    while(true)
      {
      String seq = this.readSequenceFromLocation(start, charWindow);
      writer.write(seq);
      count += seq.length();
      start += charWindow;
      if (start > end) start = end;
      if (seq.length() >= totalChars) break;
      }
    writer.flush();
    return count;
    }

  // will this hit out of mem error if the final location is too large? YEP
  /*
  Will read from whatever the last point was!
   */
  public int streamToWriter(int totalCharacters, FASTAWriter writer) throws IOException
    {
    int charactersRead = 0;
    int window = 500;
    if (window > totalCharacters) window = totalCharacters;

    while(true)
      {
      String seq = this.readSequence(window);
      writer.write(seq);
      charactersRead += seq.length();
      if ( (totalCharacters - charactersRead)/window < 1 ) window = totalCharacters - charactersRead;
      if (seq.length() < window || charactersRead == totalCharacters) break;
      }
    log.info("File loc: " + this.fileloc + " total characters read: " + charactersRead);
    writer.flush();
    return charactersRead;
    }


  /**
   * Mark the regions of "any" or N sequence starts/stops as well as gaps.
   * THIS READS THE ENTIRE FILE.  Does not keep it in memory, but for large fasta files
   * this could take a while.
   *
   * @return
   * @throws IOException
   */

  public long getLastLocation()
    {
    return this.fileloc;
    }

  private String readline() throws java.io.IOException
    {
    // Initialize our return string
    StringBuffer buf = new StringBuffer();
    // Read until a terminator or EOF are reached
    while (true)
      {
      char last = this.read();
      if (last == CARRIAGE_RETURN || last == LINE_FEED || last == RECORD_SEPARATOR || last == EOF)
        break;
      buf.append(String.valueOf(last));
      }
    return buf.toString();
    }


  private void skipline() throws java.io.IOException
    {
    // Read until a terminator or EOF are reached
    while (true)
      {
      char last = this.read();
      if (last == CARRIAGE_RETURN || last == LINE_FEED || last == RECORD_SEPARATOR || last == EOF)
        break;
      }
    }

  private char read() throws java.io.IOException
    {
    // In case of Unicode FASTA support for sequence data, break glass
    //byte read = (byte)stream.read();
    //if (read == 0x00) { read = (byte)stream.read(); }
    //return (char)read;
    char c = (char) stream.read();
    ++this.fileloc;
    return c;
    }

  /**
   * Checks to see if a given character falls out of the printable range of ASCII input
   *
   * @param c char given for value testing
   * @return boolean signaling if this character appears to be binary data instead of a printable ASCII character
   */
  private boolean smellsLikeBinaryData(char c)
    {
    return (c < 0x40 || c > 0x7E);
    }

  }
