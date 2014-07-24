package org.lcsb.lu.igcsa.mapreduce.bwa;

import net.sf.samtools.*;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.util.StringUtils;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.mapreduce.sam.SAMCoordinateWritable;

import java.io.*;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReadPairMapper extends Mapper<LongWritable, Text, Text, Text>
  {
  static Logger log = Logger.getLogger(ReadPairMapper.class.getName());

  private String bwa, reference;
  private Context context;

  public static void setReferenceName(String name, Job job)
    {
    job.getConfiguration().set("reference.fasta.name", name);
    }

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    // this allows me to shortcut the cache system when debugging in the IDE, these options should never be used otherwise
    bwa = context.getConfiguration().get("bwa.binary.path", "tools/bwa");
    reference = context.getConfiguration().get("reference.fasta.path", null);

    if (reference == null)
      {
      String refName = context.getConfiguration().get("reference.fasta.name", "?");
      if (!refName.equals("?"))
        reference = "reference/ref/" + refName;
      }
    log.info("reference: " + reference);
    }

  private String baseFileName(LongWritable key)
    {
    return context.getTaskAttemptID() + "-" + key.toString();
    }

  @Override
  protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
    this.context = context;

    File sam = new File(baseFileName(key) + ".sam");
    File fastqA = new File(baseFileName(key) + ".1.fastq");
    File fastqB = new File(baseFileName(key) + ".2.fastq");

    // this should never happen but just to check.
    if (fastqA.exists() || fastqB.exists() || sam.exists())
      throw new IOException("Cannot overwrite existing file: " + fastqA + ", " + fastqB + ", " + sam);

    BufferedOutputStream tmpWriter1 = new BufferedOutputStream(new FileOutputStream(fastqA));
    BufferedOutputStream tmpWriter2 = new BufferedOutputStream(new FileOutputStream(fastqB));

    int counter = 0;
    for (String line : value.toString().split("\n"))
      {
      if (line.equals(""))
        continue;

      String[] readInfo = line.split("\t");
      if (readInfo.length < 5)
        throw new IOException("Read line is missing information: " + line);

      ReadPairWritable read = new ReadPairWritable(readInfo);

      String read1 = read.createRead(1);
      String read2 = read.createRead(2);

      tmpWriter1.write(read1.getBytes());
      tmpWriter2.write(read2.getBytes());

      ++counter;
      }

    tmpWriter1.close();
    tmpWriter2.close();
    log.info(counter + " reads output to " + fastqA + ", " + fastqB);

    if (runAlignment(new File[]{fastqA, fastqB}, sam))
      outputSAM(sam);

    fastqA.delete();
    fastqB.delete();
    }

  private void outputSAM(File sam) throws IOException, InterruptedException
    {
    SAMFileHeader header = new SAMFileReader(sam).getFileHeader();

    for (SAMSequenceRecord seq : header.getSequenceDictionary().getSequences())
      {
      seq.getAssembly();
      seq.getSequenceName();
      }
    context.write(new Text(header.getTextHeader()), new Text());

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sam)));
    String line;
    while ((line = bufferedReader.readLine()) != null)
      {
      if (line.startsWith("@"))
        continue;

      SAMCoordinateWritable samWritable = new SAMCoordinateWritable(line);
      context.write(new Text(samWritable.getRFName()), new Text(line + "\n"));
      }
    bufferedReader.close();
    }

  private boolean runAlignment(File[] files, File sam) throws IOException, InterruptedException
    {
    String bwaAln = String.format("%s mem %s %s %s", bwa, "-t 12", reference, files[0] + " " + files[1]);

    log.info("BWA MEM: " + bwaAln);

    ByteArrayOutputStream errorOS = new ByteArrayOutputStream();
    int exitVal = new CommandExecution(context, errorOS, new FileOutputStream(sam)).execute(bwaAln);

    if (errorOS.toString().contains("fail"))
      throw new IOException("Failed to align: " + errorOS.toString());

    // this isn't necessarily wrong, esp when trying to align against derivatives
    if (exitVal > 0)
      log.error("BWA ALIGN failed: " + errorOS.toString() + "\t" + bwaAln);

    return (sam.length() > 64);
    }

  }
