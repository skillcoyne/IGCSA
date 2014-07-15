/**
 * org.lcsb.lu.igcsa.mapreduce.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.job.BWAAlign;
import org.lcsb.lu.igcsa.job.BWAIndex;
import org.lcsb.lu.igcsa.job.MiniChromosomeJob;


public class Pipeline
  {
  private static final Log log = LogFactory.getLog(Pipeline.class);

  //  # 1. Generate mini aberrations
  //  # 2. Index & Align
  //  # 3. Score
  //  # 4. Generate neighboring aberrations & ...?
  //  # 5. Repeat 2-4 until??

  public static void main(String[] args) throws Exception
    {
    if (args.length < 4)
      {
      System.err.println("Usage: Pipeline <new mini genonme name> <csv bands> <bwa archive path> <output path>  <read tsv path>");
      System.exit(2);
      }
    String genomeName = args[0]; //"SomeName";
    String bands = args[1];
    String bwaPath = args[2]; //"/tools/bwa.tgz";
    String outputPath = args[3]; // "/output/minichrs";
    String readPath = args[4]; //"/reads/ERR002980.tsv";

    outputPath = generateMiniAbrs(bwaPath, outputPath, genomeName, bands);
    log.info("Mini chrs written to " + outputPath);
    //outputPath = "/output/minichrs/SomeName/21p13-19p11";
    String fastaPath = indexMiniChrs(bwaPath, outputPath);
    //String fastaPath = "/output/minichrs/SomeName/index/all.tgz";
    log.info("FASTA index file at " + fastaPath);
    String aligned = alignReads(bwaPath, fastaPath, readPath, genomeName);
    log.info("Aligned reads written to " + aligned);
    }

  private static String generateMiniAbrs(String bwaPath, String outputPath, String genomeName, String bands) throws Exception
    {
    log.info("*********** MINI CHR JOB *************");
    MiniChromosomeJob mcj = new MiniChromosomeJob();
    ToolRunner.run(mcj, new String[]{"-p", "GRCh37", "-n", genomeName, "-o", outputPath, "-bands", bands});
    return mcj.getIndexPath().getParent().toString();
    }

  private static String indexMiniChrs(String bwaPath, String fastaPath) throws Exception
    {
    log.info("*********** INDEX CHR JOB *************");
    BWAIndex idx = new BWAIndex();
    ToolRunner.run(idx, new String[]{"-p", fastaPath, "--bwa-path", bwaPath});
    return idx.indexPath().toString();
    }

  private static String alignReads(String bwaPath, String refPath, String readPath, String genomeName) throws Exception
    {
    log.info("*********** ALIGN CHR JOB *************");
    BWAAlign ba = new BWAAlign();
    ToolRunner.run(ba, new String[]{"--bwa-path", bwaPath, "-n", genomeName, "-i", refPath, "-r", readPath, "-o", "/output/aligned"});
    ba.mergeSAM();
    return ba.getOutputPath().toString();
    }


  }
