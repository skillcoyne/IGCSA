/**
 * org.lcsb.lu.igcsa.mapreduce.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.job.*;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
      System.err.println("Usage: Pipeline <new mini genonme name> <csv chr list> <bwa archive path> <output path> <read tsv path>");
      System.exit(2);
      }
    String genomeName = args[0]; //genomeName = "SomeName";
    String chrList = args[1]; //bands = "21p13,19p11";
    String bwaPath = args[2]; //bwaPath = "/tmp/tools/bwa.tgz";
    String outputPath = args[3]; //outputPath = "/tmp/output/minichrs";
    String readPath = args[4]; //readPath = "/tmp/reads";

    List<BandGenerator.Candidate> bandList = selectBands(chrList.split(","));

    log.info(bandList);
    String aligned = null;
    //bandList = "21p13,19p11;22q12,18q23";
    for (BandGenerator.Candidate cand: bandList)
      {
      String bands = cand.getBands().get(0).getFullName() + "," + cand.getBands().get(1).getFullName();
      String chrOutputPath = generateMiniAbrs(bwaPath, outputPath, genomeName, bands);
      log.info("Mini chrs written to " + chrOutputPath);
      //outputPath = "/output/minichrs/SomeName/21p13-19p11";
      String fastaPath = indexMiniChrs(bwaPath, chrOutputPath);

      //String fastaPath = "/output/minichrs/" + genomeName + "/" + bands.replace(",","-") + "/index/all.tgz";
      log.info("FASTA index file at " + fastaPath);

      aligned = alignReads(bwaPath, fastaPath, readPath, genomeName, "s3n://insilico/output/aligned/" + genomeName + "/" + bands.replace(',', '-') );
      log.info("Aligned reads written to " + aligned);
      }
    if (aligned != null)
      {
      String scores = scoreReads("s3n://output/aligned/" + genomeName, "s3n://scoring/" + genomeName);
      log.info("Scores in " + scores);
      }
    }

  private static List<BandGenerator.Candidate> selectBands(String... chrs) throws ProbabilityException, IOException
    {
    int max = 2;
    BandGenerator bg = new BandGenerator();
    bg.run(chrs[0], chrs[1]);
    log.info(bg.getCandidates().size());

    List<BandGenerator.Candidate> candidateList;
    if (bg.getCandidates().size() <= max)
      candidateList = bg.getCandidates();
    else
      candidateList = bg.getTopCandidates(max);

      return candidateList;
//    List<String> bandList = new ArrayList<String>();
//    for (BandGenerator.Candidate candidate: candidateList)
//
//      bandList.add(StringUtils.join(candidate.getBands().iterator(), ","));
//
//    return bandList;
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

  private static String alignReads(String bwaPath, String refPath, String readPath, String genomeName, String output) throws Exception
    {
    log.info("*********** ALIGN CHR JOB *************");
    BWAAlign ba = new BWAAlign();
    ToolRunner.run(ba, new String[]{"--bwa-path", bwaPath, "-n", genomeName, "-i", refPath, "-r", readPath, "-o", output});
    ba.mergeSAM();
    return ba.getOutputPath().toString();
    }

  private static String scoreReads(String samPath, String outputPath) throws Exception
    {
    log.info("*********** SCORE ALIGNMENT *************");
    ScoreSAMJob ssj = new ScoreSAMJob();
    ToolRunner.run(ssj,new String[]{"-p", samPath, "-o", outputPath}  );
    ToolRunner.run(new ScoreBandRatios(ssj.getConf()), new String[]{outputPath + "/score-files.txt"});
    return ssj.getOutputPath().toString();
    }


  }
