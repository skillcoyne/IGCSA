package org.lcsb.lu.igcsa.mapreduce.bwa;

import net.sf.samtools.*;
import org.apache.commons.io.FileUtils;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.ThreadedStreamConnector;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReadPairMapper extends Mapper<LongWritable, Text, Text, Text>
  {
  static Logger log = Logger.getLogger(ReadPairMapper.class.getName());

  private Context context;
  private String bwa, reference;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    bwa = context.getConfiguration().get("bwa.binary.path", "tools/bwa");
    reference = context.getConfiguration().get("reference.fasta.path", "reference/ref/reference.fa");

    this.context = context;

    File bwaBinary = new File(bwa);
    if (!bwaBinary.exists())
      {
      bwa = "/usr/local/bin/bwa"; // to run in the IDE?
      reference = "/tmp/test6/ref/reference.fa";
      //throw new RuntimeException("bwa binary does not exist in the cache.");
      }
    log.info("BWA BINARY FOUND: " + bwaBinary);
    }

  private String baseFileName(LongWritable key)
    {
    return context.getTaskAttemptID() + "-" + key.toString();
    }

  @Override
  protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
    File[] readA = new File[]{new File(baseFileName(key) + ".1.fastq"), new File(baseFileName(key) + ".1.sai")};
    File[] readB = new File[]{new File(baseFileName(key) + ".2.fastq"), new File(baseFileName(key) + ".2.sai")};
    File sam = new File(baseFileName(key) + ".sam");

    File fastqA = readA[0];
    File fastqB = readB[0];

    BufferedOutputStream tmpWriter1 = new BufferedOutputStream(new FileOutputStream(fastqA));
    BufferedOutputStream tmpWriter2 = new BufferedOutputStream(new FileOutputStream(fastqB));

    int counter = 0;
    for (String line : value.toString().split("\n"))
      {
      if (line.equals("")) log.info("foo");
      ReadPairWritable read = new ReadPairWritable(line.split("\t"));

      String read1 = read.createRead(1);
      String read2 = read.createRead(2);

      tmpWriter1.write(read1.getBytes());
      tmpWriter2.write(read2.getBytes());

      ++counter;
      }

    tmpWriter1.close();
    tmpWriter2.close();
    log.info(counter + " reads output to " + fastqA + ", " + fastqB);

    if (runAlignment(readA) && runAlignment(readB))
      {
      pairedEnd(readA, readB, sam);

      log.info("TEMP SAM FILE: " + sam.getAbsolutePath());
      readSam(sam, context);
      }
    else log.error("ALIGN FAILED");
    }

  private void readSam(File sam, Context context) throws IOException, InterruptedException
    {


    SAMFileHeader header = new SAMFileReader(sam).getFileHeader();

    for (SAMSequenceRecord seq: header.getSequenceDictionary().getSequences())
      {
      seq.getAssembly();
      seq.getSequenceName();
      }
    context.write(new Text(header.getTextHeader()), new Text());

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sam)));
    String line;
    while ((line = bufferedReader.readLine()) != null)
      {
      if (line.startsWith("@")) continue;

      SAMCoordinateWritable samWritable = new SAMCoordinateWritable(line);
      context.write( new Text(samWritable.getRFName()), new Text(line + "\n"));
      }
    bufferedReader.close();
    }

private boolean runAlignment(File[] files) throws IOException, InterruptedException
  {
  File fastq = files[0];
  File sai = files[1];

  String bwaAln = String.format("%s aln %s %s %s", bwa, "-q 15", reference, fastq);

  log.info("BWA ALN: " + bwaAln);

  Thread error, out;
  Process p = Runtime.getRuntime().exec(bwaAln);
  // Reattach stderr and write System.stdout to tmp file
  //error = new Thread(new ThreadedStreamConnector(p.getErrorStream(), System.err)
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  error = new Thread(new ThreadedStreamConnector(p.getErrorStream(), baos)
  {
  @Override
  public void progress()
    {
    context.progress();
    }
  });
  FileOutputStream fout = new FileOutputStream(sai);
  out = new Thread(new ThreadedStreamConnector(p.getInputStream(), fout));
  out.start();
  out.join();
  error.start();
  error.join();

  log.info(baos.toString());

  int exitVal = p.waitFor();
  fout.close();
  baos.close();

  log.info("SAI FILE SIZE: " + sai.length());

  if (exitVal > 0) throw new IOException("Alignment failed: " + baos.toString());
  return (sai.length() > 64);
  }

private void pairedEnd(File[] read1, File[] read2, File sam) throws IOException, InterruptedException
  {
  String bwaSampe = String.format("%s sampe %s %s %s %s %s %s", bwa, "", reference, read1[1], read2[1], read1[0], read2[0]);

  log.info("BWA SAMPE: " + bwaSampe);

  Thread error, out;
  Process p = Runtime.getRuntime().exec(bwaSampe);

  // Reattach stderr and write System.stdout to tmp file
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  error = new Thread(new ThreadedStreamConnector(p.getErrorStream(), baos)
  {
  @Override
  public void progress()
    {
    context.progress();
    }
  });
  FileOutputStream fout = new FileOutputStream(sam);
  out = new Thread(new ThreadedStreamConnector(p.getInputStream(), fout));
  out.start();
  out.join();
  error.start();
  error.join();

  log.info(baos.toString());

  int exitVal = p.waitFor();
  fout.close();
  baos.close();

  log.info("SAM SIZE: " + sam.length());

  if (exitVal > 0) throw new RuntimeException("BWA sampe failed: " + baos.toString());
  }

}
