package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;


import java.io.*;
import java.util.zip.GZIPInputStream;

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

  private File fasta;

  private BufferedFASTAReader reader;


  public FASTAReader(File file) throws IOException
    {
    fasta = file;
    if (!fasta.exists())
      throw new FileNotFoundException("No such file: " + file.getAbsolutePath());
    if (!fasta.canRead())
      throw new IOException("Cannot read file " + file.getAbsolutePath());

    openReader();
    }


  private void openReader() throws IOException
    {
    if (reader != null) reader.close();

    InputStream is = new FileInputStream(this.fasta);
    if (this.fasta.getName().endsWith("gz")) is = new GZIPInputStream(is);

    reader = new BufferedFASTAReader(new InputStreamReader(is));
    }


  public FASTAHeader getHeader() throws IOException
    {
    return reader.getHeader();
    }

  public void reset() throws IOException
    {
    log.info("Resetting reader");
    openReader();
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
    if (start < reader.getCurrentSequenceLocation())
      openReader();

    log.debug("file location "+ reader.getCurrentSequenceLocation());
    reader.skip(start);

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
    String seq = buf.toString();
    if (seq.length() <= 0) seq = null;

    return seq;
    }

  /*
 Will read from whatever the last point was!
  */
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
    return reader.getCurrentSequenceLocation();
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
    char c = (char) reader.read();
    return c;
    }

  /**
   * Checks to see if a given character falls out of the printable range of ASCII input
   *
   * @param c char given for value testing
   * @return boolean signaling if this character appears to be binary data instead of a printable ASCII character
   */
//  private boolean smellsLikeBinaryData(char c)
//    {
//    return (c < 0x40 || c > 0x7E);
//    }

  }
