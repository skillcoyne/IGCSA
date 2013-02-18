package org.lcsb.lu.igcsa.fasta;

import org.apache.commons.lang.ArrayUtils;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static org.lcsb.lu.igcsa.fasta.NucleotideCodes.*;
import static org.lcsb.lu.igcsa.fasta.NucleotideCodes.getNucleotideCodesMap;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAReader
  {
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

  private File fasta;
  private InputStream stream;
  private FASTAHeader header;

  // not really sure this is useful
  private static final Map<Character, NucleotideCodes> nucleotideCodesMap = getNucleotideCodesMap();

  private Collection<Location> repeatRegions = new ArrayList<Location>();
  private Collection<Location> gapRegions = new ArrayList<Location>();


  public FASTAReader(File file) throws FileNotFoundException, IOException, Exception
    {
    fasta = file;
    if (!fasta.exists()) throw new FileNotFoundException("No such file: " + file.getAbsolutePath());
    if (!fasta.canRead()) throw new IOException("Cannot read file " + file.getAbsolutePath());
    }

  public void open() throws IOException
    {
    this.fileloc = 0L;
    this.stream = new FileInputStream(this.fasta);
    if (this.fasta.getName().endsWith("gz"))
      {
      stream = new GZIPInputStream(stream);
      }
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

  public void close() throws IOException
    {
    stream.close();
    }


  /**
   * This method is primarily used to pull sections of sequence, for instance from specific bands
   * for use in generating mutated chromosomes.
   *
   * @param start
   * @param end
   * @return
   * @throws IOException
   */
  //  public String readSequence(long start, long end) throws IOException
  //    {
  //    if (end < start) throw new IOException("End location comes before start location.");
  //    long length = end - start;
  //    byte[] buf = new byte[(int) length];
  //    RandomAccessFile ra = new RandomAccessFile(fasta, "r");
  //    ra.seek(start);
  //    int bytesRead = ra.read(buf);
  //    //System.out.println("" + bytesRead);
  //    //buf = ArrayUtils.subarray(buf, 0, bytesRead);
  //    this.fileloc = ra.getChannel().position();
  //    ra.close();
  //    return new String(ArrayUtils.subarray(buf, 0, bytesRead));
  //    }
  //
  //  public String readSequence(long start, long end, boolean headerOffset) throws IOException
  //    {
  //    if (headerOffset)
  //      {
  //      start += headerloc;
  //      end += headerloc;
  //      }
  //    return readSequence(start, end);
  //    }


  /**
   * Read and return chunks from the file. Each call will advance the read.
   *
   * @param window
   * @return
   * @throws IOException
   */
  public String readSequence(int window) throws IOException
    {
    if (stream == null) open();
    StringBuffer buf = new StringBuffer(window);
    char c;
    while ((c = this.read()) != EOF)
      {
      // header has already been read
      if (c == HEADER_IDENTIFIER)
        {
        if (this.header == null) getHeader();
        else this.skipline();
        continue;
        }
      // don't want the line separators
      if (c == CARRIAGE_RETURN || c == LINE_FEED) continue;
      // shouldn't be an issue but it would mean we're done reading
      if (c == RECORD_SEPARATOR) break;
      buf.append(Character.toString(c));
      if (buf.length() == window) break;
      }
    return buf.toString();
    }


  /**
   * Mark the regions of "any" or N sequence starts/stops as well as gaps.
   *
   * @return
   * @throws IOException
   */
  public void markRegions() throws IOException, Exception
    {
    open();
    int repeatStart = 0;
    int gapStart = 0;
    char lastChar = '&'; // unlikely to be found in a fasta file that I am aware of
    char c;
    while ((c = this.read()) != EOF)
      {
      // header has already been read
      if (c == HEADER_IDENTIFIER)
        {
        if (this.header == null) getHeader();
        else this.skipline();
        }

      if (c == N.getNucleotide() && c != lastChar)
        {
        repeatStart = (int) this.fileloc;
        }
      else if (lastChar == N.getNucleotide() && c != lastChar)
        {
        this.repeatRegions.add(new Location(repeatStart, (int) this.fileloc));
        repeatStart = 0;
        }
      else if (c == GAP.getNucleotide() && c != lastChar)
        {
        gapStart = (int) this.fileloc;
        }
      else if (lastChar == GAP.getNucleotide() && c != lastChar)
        {
        this.gapRegions.add(new Location(gapStart, (int) this.fileloc));
        gapStart = 0;
        }
      lastChar = c;
      }
    stream.close();
    open();
    }

  public long getLastLocation()
    {
    return this.fileloc;
    }

  public Location[] getRepeatRegions()
    {
    return repeatRegions.toArray(new Location[repeatRegions.size()]);
    }

  public Location[] getGapRegions()
    {
    return gapRegions.toArray(new Location[gapRegions.size()]);
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
