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
    if (!fasta.exists()) throw new FileNotFoundException("No such file: " + file.getAbsolutePath());
    if (!fasta.canRead()) throw new IOException("Cannot read file " + file.getAbsolutePath());

    openReader();
    }


  private void openReader() throws IOException
    {

    if (this.reader != null) reader.close();

    InputStream is = new FileInputStream(this.fasta);
    if (this.fasta.getName().endsWith("gz")) is = new GZIPInputStream(is);

    reader = new BufferedFASTAReader(new InputStreamReader(is));
    log.debug("Opening FASTAReader for " + this.fasta.getAbsolutePath() + ", file location " + reader.getCurrentSequenceLocation());
    }


  public FASTAHeader getHeader() throws IOException
    {
    return reader.getHeader();
    }

  public void reset() throws IOException
    {
    openReader();
    }

  /**
   * This method creates a new BufferedReader with each call so theoretically it should be capable of being
   * called on the same FASTAReader object
   *
   * @param start - Offset to start reading file from. The header offset will be added to this so the header line is always skipped.
   * @return String containing the characters read. If the end of the file is found before the total number of characters is reached
   *         the characters read to that point will be returned.
   * @throws IOException
   */
  public String readSequenceFromLocation(int start, int window) throws IOException
    {
    if (start == 0) start = 1;
    if (start < this.reader.getCurrentSequenceLocation()) openReader();

    log.debug("file location " + reader.getCurrentSequenceLocation());
    long skipped = reader.skip(start);
    log.debug("skipped: " + skipped);

    return this.readSequence(window);
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
    if (window <= 0) return null;

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
      if (c == CARRIAGE_RETURN || c == LINE_FEED) continue;
      // shouldn't be an issue but it would mean we're done reading
      if (c == RECORD_SEPARATOR) break;
      buf.append(Character.toString(c));
      if (buf.length() == window) break;
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
    log.info("streaming to writer " + start + " " + end);
    int charWindow = 1000;
    int totalChars = end - start;

    int count = 0;
    if (charWindow > totalChars) charWindow = totalChars;

    while (true)
      {
      String seq = this.readSequenceFromLocation(start, charWindow);
      if (seq == null) return 0;

      writer.write(seq);
      count += seq.length();
      start += charWindow+1;
      if (start > end) start = end;
      if ((end - start) < charWindow) charWindow = end - start;
      if (count >= totalChars || seq.length() < charWindow) break;
      }

    return count;
    }

  /*
  Read from last point and output directly to the writer.
   */
  public int streamToWriter(int charToRead, FASTAWriter writer) throws IOException
    {
    long window = 1000;
    if (charToRead < window)
      window = charToRead;

    int count = 0;
    String seq;
    while (window > 0 && (seq = readSequence((int) window)) != null )
      {
      writer.write(seq);
      count += seq.length();
      if (charToRead - count < window)
        window = charToRead - count;
      }
    return count;
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
      if (last == CARRIAGE_RETURN || last == LINE_FEED || last == RECORD_SEPARATOR || last == EOF) break;
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
      if (last == CARRIAGE_RETURN || last == LINE_FEED || last == RECORD_SEPARATOR || last == EOF) break;
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
