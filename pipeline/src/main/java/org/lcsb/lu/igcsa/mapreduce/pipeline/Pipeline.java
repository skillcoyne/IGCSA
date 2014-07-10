/**
 * org.lcsb.lu.igcsa.mapreduce.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.BWAAlign;
import org.lcsb.lu.igcsa.BWAIndex;
import org.lcsb.lu.igcsa.MiniChromosomeJob;


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
      System.err.println("Usage: Pipeline <new mini genonme name> <bwa archive path> <output path>  <read tsv path>");
      System.exit(2);
      }
    String genomeName = args[0]; //"SomeName";
    String bwaPath = args[1]; //"/tools/bwa.tgz";
    String outputPath = args[2]; // "/output/minichrs";
    String readPath = args[3]; //"/reads/ERR002980.tsv";

    outputPath = generateMiniAbrs(bwaPath, outputPath, genomeName);
    log.info("Mini chrs written to " + outputPath);
//    String fastaPath = indexMiniChrs(bwaPath, outputPath);
//    log.info("FASTA index file at " + fastaPath);
//    String aligned = alignReads(bwaPath, fastaPath, readPath, genomeName);
//    log.info("Aligned reads written to " + aligned);
    }

  private static String generateMiniAbrs(String bwaPath, String outputPath, String genomeName) throws Exception
    {
    log.info("*********** MINI CHR JOB *************");
    MiniChromosomeJob.main(new String[]{"-p", "GRCh37", "-n", genomeName, "-o", outputPath, "-b", "1p33,10p13"});
    return outputPath + "/" + genomeName;
    }

  private static String indexMiniChrs(String bwaPath, String fastaPath) throws Exception
    {
    log.info("*********** INDEX CHR JOB *************");
    BWAIndex.main(new String[]{"-p", fastaPath, "-b", bwaPath});
    return fastaPath + "/index/all.tgz";
    }

  private static String alignReads(String bwaPath, String refPath, String readPath, String genomeName) throws Exception
    {
    log.info("*********** ALIGN CHR JOB *************");
    BWAAlign.main(new String[]{"-b", bwaPath, "-n", genomeName, "-i", refPath, "-r", readPath, "-o", "/output/aligned"});
    return "/output/aligned";
    }


  }
