/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.*;

import java.io.File;
import java.io.IOException;


public class FASTAUtil
  {
  private static final Log log = LogFactory.getLog(FASTAUtil.class);


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

  public static void mergeFASTASegments(FileSystem fs, Path dir, String destFile) throws IOException
    {
    FSDataOutputStream os = fs.create(new Path(dir, destFile));

    for (FileStatus status : fs.listStatus(dir))
      {
      if (status.getPath().getName().startsWith("_") || status.getPath().getName().startsWith("."))
        continue;

      writeContents(fs.open(status.getPath()), os);

      os.flush();
      log.info(status);
      }

    os.flush();
    os.close();
    }

  public static void copyFile(FileSystem fs, Path srcFile, Path destFile) throws IOException
    {
    FSDataOutputStream os = fs.create(destFile);
    writeContents(fs.open(srcFile), os);

    os.flush();
    os.close();
    }

  private static void writeContents(FSDataInputStream is, FSDataOutputStream os) throws IOException
    {
    int c;
    while ((c = is.read()) != -1)
      os.write(c);
    is.close();
    }


  public static void deleteCRCFile(FileSystem fs, Path dir, String fileName) throws IOException
    {
    fs.delete(new Path(dir, "." + fileName + ".crc"), false);
    }

  public static void moveFile(Path srcDir, String src, String dest) throws IOException
    {
    FileUtil.replaceFile(new File(srcDir.toString(), src), new File(srcDir.toString(), dest));
    }

  }
