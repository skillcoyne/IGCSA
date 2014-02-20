/**
 * org.lu.igcsa.hadoop.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CharacterReader
  {
  private static final Log log = LogFactory.getLog(CharacterReader.class);

  private static final int DEFAULT_BUFFER_SIZE = 65536;

  // The standard gamut of line terminators, plus EOF
  private static final char CR = 0x000A;
  // ASCII carriage return (CR), as char
  private static final char LF = 0x000D;
  // ASCII line feed (LF), as char
  private static final char RECORD_SEPARATOR = 0x001E;
  // ASCII record separator (RS), as char
  private static final char EOF = 0xffff;

  private InputStream in;

  // in characters NOT bytes
  private static long filePos = 0L;
  private static long numChars = 0L;

  private BufferedReader bufferedReader;

  public CharacterReader(InputStream in)
    {
    this(in, 0);
    }

  public CharacterReader(InputStream in, long startChar)
    {
    log.info("*** NEW READER ***");
    this.bufferedReader = new BufferedReader(new InputStreamReader(in));
    this.in = in;
    filePos += startChar;
    try
      {
      bufferedReader.skip(startChar);
      }
    catch (IOException e)
      {
      e.printStackTrace();
      }
    }
  public String readLine() throws IOException
    {
    String line = bufferedReader.readLine();
    if (line != null)
      {
      numChars += line.length();
      filePos += line.length() + 1;
      }
    return line;
    }

  public String readCharacters(int maxChars) throws IOException
    {
    if (maxChars <= 0) return null;

    StringBuffer buf = new StringBuffer();
    char c;
    while ((c = this.read()) != EOF)
      {
      // don't want the line separators
      if (c == CR || c == LF) continue;
      // shouldn't be an issue but it would mean we're done reading
      if (c == RECORD_SEPARATOR) break;
      buf.append(Character.toString(c));

      if (buf.length() == maxChars) break;
      }

    String str = buf.toString();
    if (str.length() <= 0)
      str = null;

    return str;
    }

  public long getNumChars()
    {
    return numChars;
    }

  public long getPos()
    {
    return filePos;
    }

//  public void skip(int nChar) throws IOException
//    {
//    bufferedReader.skip(nChar);
//    filePos += nChar;
//    }

  public void reset() throws IOException
    {
    bufferedReader.reset();
    filePos = 0L;
    numChars = 0L;
    }

  public void close() throws IOException
    {
    bufferedReader.close();
    in.close();
    }

  protected char read() throws IOException
    {
    char c = (char) bufferedReader.read();
    ++filePos;
    if (c != CR && c != LF)
      ++numChars;
    return c;
    }

  }
