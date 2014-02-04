package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.commons.io.FileUtils;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.ThreadedStreamConnector;

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
    if (value.getLength() > 1) // this is a hack to overcome what may be an issue running in single-node mode
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

      //FileUtils.copyFile(fastqA, new File("/tmp", fastqA.getName()));

      if (runAlignment(readA) && runAlignment(readB))
        {
        pairedEnd(readA, readB, sam);
        log.info("TEMP SAM FILE: " + sam.getAbsolutePath());

        Text headerKey = new Text(); // sam header
        Text samValue = new Text();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sam)));
        String line;
        while ((line = bufferedReader.readLine()) != null)
          {
          System.err.println(line);
          if (line.startsWith("@")) headerKey.append((line + "\n").getBytes(), 0, line.length() + 1);
          else samValue.append((line + "\n").getBytes(), 0, line.length() + 1);
          }

        //log.info("REDUCE key:" + headerKey + " VALUE: " + samValue);
        context.write(headerKey, samValue);
        }
      else log.error("ALIGN FAILED");
      }
    }


  private boolean runAlignment(File[] files) throws IOException, InterruptedException
    {
    File fastq = files[0];
    File sai = files[1];

    //log.info("FILES: " + fastq.getAbsolutePath() + "\n" + sai.getAbsolutePath());

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

    //System.err.println(baos.toString());

    int exitVal = p.waitFor();
    fout.close();
    baos.close();

    log.info("SAI FILE SIZE: " + sai.length());

    //FileUtils.copyFile(sai, new File("/tmp", sai.getName()));

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

    //System.err.println(baos.toString());

    int exitVal = p.waitFor();
    fout.close();
    baos.close();

    log.info("SAM SIZE: " + sam.length());

    //FileUtils.copyFile(sam, new File("/tmp", sam.getName()));

    if (exitVal > 0) throw new RuntimeException("BWA sampe failed: " + baos.toString());
    }

  }
