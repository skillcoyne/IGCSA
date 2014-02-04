package org.lcsb.lu.igcsa;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.fs.FileSystem;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FastqToTSV
  {
  static Logger log = Logger.getLogger(FastqToTSV.class.getName());

  private File readFile1, readFile2;
  private BufferedReader reader1, reader2;

  private FileSystem fs;

  private String fastqName;

  private char sep = '\t';

  public FastqToTSV(File readFile1, File readFile2)
    {
    this.readFile1 = readFile1;
    this.readFile2 = readFile2;
    if (readFile1.length() != readFile2.length())
      log.warn("Read pair files are different sizes. Some data may be lost.");

    fastqName = readFile1.getName().substring(0, readFile1.getName().indexOf("_"));
    }

  public Path toTSV(FileSystem fs, Path outputPath) throws IOException
    {
    Path tsvFile = new Path(outputPath, fastqName + ".tsv");

    openReaders();
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(tsvFile, true)));

    String line1, line2;
    while ( (line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null )
      {
      if (!line1.startsWith("@") || !line2.startsWith("@"))
        throw new IOException("Read files may be corrupted as reads are not matching up.");

      Pattern p = Pattern.compile("(.*)\\/\\d+$");

      Matcher matcher = p.matcher(line1);
      if (!matcher.matches() || !p.matcher(line2).matches())
        throw new IOException("Reads are not mate-pairs (" + line1 + " : " + line2 + ")");

      String readName = matcher.group(1);

      writer.write(readName + sep + getRead(reader1) + sep + getRead(reader2) + "\n");
      }
    writer.close();

    return tsvFile;
    }

  private String getRead(BufferedReader reader) throws IOException
    {
    String sequence = reader.readLine();
    // skip the + line
    String qual = reader.readLine();
    if (qual.equals("+"))
      qual = reader.readLine();

    return sequence + sep + qual;
    }


  private void openReaders() throws FileNotFoundException
    {
    reader1 = new BufferedReader(new FileReader(readFile1));
    reader2 = new BufferedReader(new FileReader(readFile2));
    }


  }
