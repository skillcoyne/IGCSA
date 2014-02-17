/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.fasta;

import com.m6d.filecrush.crush.Crush;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.util.ToolRunner;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FASTAUtil
  {
  private static final Log log = LogFactory.getLog(FASTAUtil.class);

  // The standard gamut of line terminators, plus EOF
  public static final char CARRIAGE_RETURN = 0x000A;
  // ASCII carriage return (CR), as char
  public static final char LINE_FEED = 0x000D;
  // ASCII line feed (LF), as char
  public static final char RECORD_SEPARATOR = 0x001E;
  // ASCII record separator (RS), as char
  public static final char EOF = 0xffff;

  // Reserved characters within the (ASCII) stream
  public static final char COMMENT_IDENTIFIER = ';';
  public static final char HEADER_IDENTIFIER = '>';

  public static void deleteChecksumFiles(FileSystem fs, Path dir) throws IOException
    {
    log.debug("Deleting CRC files");
    for (FileStatus status : fs.listStatus(dir))
      {
      log.info(status.getPath());
      Path crcFile = new Path(dir, "." + status.getPath().getName() + ".crc");
      if (fs.exists(crcFile))
        {
        log.info("deleting " + crcFile.toString());
        fs.delete(crcFile, false);
        }

      if (status.getPath().getName().equals("_SUCCESS"))
        {
        log.info("deleting " + status.getPath().toString());
        fs.delete(status.getPath(), false);
        }
      }
    }

  public static String getChromosomeFromFASTA(String fileName) throws IOException
    {
    Pattern p = Pattern.compile("^.*(\\d+|X|Y)\\.fa.*$");
    Matcher matcher = p.matcher(fileName);

    if (matcher.find())
      return matcher.group(1);
    else
      throw new IOException(fileName + " does not contain a chromosome.");
    }

  // Create a single merged FASTA file from files in the src directory.
  public static void mergeFASTAFiles(FileSystem fs, String src, String dest) throws Exception
    {
    log.info("Merging files in " + src);
    ToolRunner.run(new Crush(), new String[]{"--input-format=text", "--output-format=text", "--compress=none", src, dest});
    }

  public static String fastaFileList(FileSystem fs, PathFilter filter, Path path, String name) throws IOException
    {
    Path outputFile = new Path(path.getParent(), name);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(outputFile, true)));

    int i = 1;
    for (FileStatus status: fs.listStatus(path, filter))
      {
      String writePath = status.getPath().toUri().toASCIIString().replace(fs.getUri().toASCIIString(), "");
      if (!writePath.startsWith("/"))
        writePath = "/" + writePath;
      writer.write( i + "\t" + writePath + "\n" );
      ++i;
      }
    writer.close();

    return outputFile.toString();
    }


  }
