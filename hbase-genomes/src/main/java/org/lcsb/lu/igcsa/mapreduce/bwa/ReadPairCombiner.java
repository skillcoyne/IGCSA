package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.ThreadedStreamConnector;

import java.io.*;
import java.util.Iterator;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReadPairCombiner extends Reducer<LongWritable, ReadPairWritable, Text, Text>
  {
  static Logger log = Logger.getLogger(ReadPairCombiner.class.getName());

  private Context context;
  private String bwa;
  private String reference;


  private String baseFileName(LongWritable key)
    {
    return context.getTaskAttemptID() + "-" + key.toString();
    }

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    this.context = context;

    bwa = context.getConfiguration().get("bwa.binary.path", "tools/bwa");
    reference = context.getConfiguration().get("reference.fasta.path", "reference/ref/reference.fa");

    //      File bwaBinary = new File(bwa);
    //      if (!bwaBinary.exists()) throw new RuntimeException("bwa binary does not exist in the cache.");
    //      log.info("BWA BINARY FOUND: " + bwaBinary);
    }

  @Override
  protected void reduce(LongWritable key, Iterable<ReadPairWritable> values, Context context) throws IOException, InterruptedException
    {
    File[] readA = new File[]{new File(baseFileName(key) + ".1.fastq"), new File(baseFileName(key) + ".1.sai")};
    File[] readB = new File[]{new File(baseFileName(key) + ".2.fastq"), new File(baseFileName(key) + ".2.sai")};
    File sam = new File(baseFileName(key) + ".sam");

    Iterator<ReadPairWritable> tI = values.iterator();
    int counter = 0;

    BufferedOutputStream tmpWriter1 = new BufferedOutputStream(new FileOutputStream(readA[0]));
    BufferedOutputStream tmpWriter2 = new BufferedOutputStream(new FileOutputStream(readB[0]));

    while (tI.hasNext())
      {
      ReadPairWritable read = tI.next();

      String read1 = read.createRead(1);
      String read2 = read.createRead(2);

      tmpWriter1.write(read1.getBytes());
      tmpWriter2.write(read2.getBytes());

      ++counter;
      }

    tmpWriter1.close();
    tmpWriter2.close();

    FileUtils.copyFile(readA[0], new File("/tmp", readA[0].getName()));

    log.info("TOTAL READS FOR KEY " + key + ": " + counter + " file size: " + readA[0].length() + " " + readB[0].length());

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
        log.info(line);
        if (line.startsWith("@")) headerKey.append((line + "\n").getBytes(), 0, line.length() + 1);
        else samValue.append((line + "\n").getBytes(), 0, line.length() + 1);
        }

      log.info("REDUCE key:" + headerKey + " VALUE: " + samValue);

      context.write(headerKey, samValue);
      }
    else
      log.error("ALIGN FAILED");

    }

  private boolean runAlignment(File[] files) throws IOException, InterruptedException
    {
    File fastq = files[0];
    File sai = files[1];

    log.info("FILES: " + fastq.getAbsolutePath() + "\n" + sai.getAbsolutePath());

    String bwaAln = String.format(
                    "%s aln %s %s %s",
                    bwa, "-q 15", reference, fastq );

    //String bwaCmd = bwa + " aln " + reference + " " + fastq.toString();
    log.info("BWA ALN: " + bwaAln);

    Thread error, out;
    Process p = Runtime.getRuntime().exec(bwaAln);

    // TODO apparently I am not understanding how this works and so I'm not capturing data in the sai or sam files (obviously sai is the first issue)

    // Reattach stderr and write System.stdout to tmp file
    error = new Thread(new ThreadedStreamConnector(p.getErrorStream(), System.err)
    {
    @Override
    public void progress()
      {
      context.progress();
      }
    });
    FileOutputStream fout = new FileOutputStream(sai);
    out = new Thread(new ThreadedStreamConnector(p.getInputStream(), fout));
    error.start(); out.start();
    error.join(); out.join();

    int exitVal = p.waitFor();
    fout.close();

    log.info("SAI FILE SIZE: " + sai.length());

    if (exitVal > 0)
      throw new IOException("Alignment failed: " + bwaAln);
    return (sai.length() > 64);
    }

  private void pairedEnd(File[] read1, File[] read2, File sam) throws IOException, InterruptedException
    {
    String[] bwaCmdArgs = new String[]{bwa, "sampe", reference, read1[1].toString(), read2[1].toString(), read1[0].toString(),
        read2[0].toString()};

    String bwaCmd = StringUtils.join(bwaCmdArgs, " ");
    log.info("BWA SAMPE: " + bwaCmd);

    Thread error, out;
    Process p = Runtime.getRuntime().exec(bwaCmd);

    // Reattach stderr and write System.stdout to tmp file
    error = new Thread(new ThreadedStreamConnector(p.getErrorStream(), System.err)
    {
    @Override
    public void progress()
      {
      context.progress();
      }
    });
    FileOutputStream fout = new FileOutputStream(sam);
    //      OutputStream os = new PrintStream(new FileOutputStream(FileDescriptor.out));
    //org.apache.commons.io.output.ByteArrayOutputStream os = new ByteArrayOutputStream();
    out = new Thread(new ThreadedStreamConnector(p.getInputStream(), fout));


    error.start();
    out.start();
    error.join();
    out.join();

    int exitVal = p.waitFor();
    fout.close();
    //os.close();

    log.info("SAM SIZE: " + sam.length());
    //log.error("OUTPUT STREAM SAM:" + os.toString());

    if (exitVal > 0) throw new RuntimeException("BWA sampe failed: " + bwaCmd);
    }


  }
