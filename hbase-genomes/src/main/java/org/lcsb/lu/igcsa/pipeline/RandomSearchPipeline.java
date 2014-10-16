/**
 * org.lcsb.lu.igcsa.mapreduce.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.cli.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.population.MinimalKaryotype;
import org.lcsb.lu.igcsa.population.PopulationGenerator;
import org.lcsb.lu.igcsa.job.*;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;

import java.util.*;


//** Need to update this to be a pipeline that starts from random selection
public class RandomSearchPipeline
  {
  private static final Log log = LogFactory.getLog(RandomSearchPipeline.class);

  public RandomSearchPipeline()
    {

    }

  //  # 1. Generate mini aberrations
  //  # 2. Index & Align
  //  # 3. Score
  //  # 4. Generate neighboring aberrations & ...?
  //  # 5. Repeat 2-4 until??

  public static CommandLine parseCommandLine(String[] args) throws ParseException
    {
    Options options = new Options();
    options.addOption(new Option("b", "bwa-path", true, "bwa archive location"));
    options.addOption(new Option("o", "output", true, "output path"));
    options.addOption(new Option("g", "genome", true, "parent genome name for sequence generation"));
    options.addOption(new Option("r", "reads", true, "read path for tsv"));

    CommandLine cl = new BasicParser().parse(options, args);

    HelpFormatter help = new HelpFormatter();
    if (!cl.hasOption("b") | !cl.hasOption("o") | !cl.hasOption("g") | !cl.hasOption("r"))
      {
      help.printHelp(LocalSearchPipeline.class.getSimpleName() + ":\nMissing required option. ", options);
      System.exit(-1);
      }

    return cl;
    }


  public static void main(String[] args) throws Exception
    {
    CommandLine cl = parseCommandLine(args);

    List<MinimalKaryotype> pop = new PopulationGenerator().run(1000, 75);
    Set<Aberration> randomBandPairSet = new HashSet<Aberration>();
    for (MinimalKaryotype mk : pop)
      {
      for (Aberration abr : mk.getAberrations())
        {
        if (abr.getAberration().equals(AberrationTypes.TRANSLOCATION))
          randomBandPairSet.add(abr);
        }
      }

    for (Aberration abr: randomBandPairSet)
      {
      String indexPath = generateMiniAbrs(cl, abr);
      String alignPath = alignReads(cl, indexPath);
      log.info(alignPath);
      break;
      }

    }

  private static String generateMiniAbrs(CommandLine cl, Aberration abr) throws Exception
    {
    List<String> bands = new ArrayList<String>();
    for (Band band: abr.getBands())
      {
      bands.add("-band");
      bands.add(band.getFullName());
      }

    log.info("*********** MINI CHR JOB " + bands + " *************");

    MiniChromosomeJob mcj = new MiniChromosomeJob();
    ToolRunner.run(mcj, (String[]) ArrayUtils.addAll(new String[]{
        "-b", cl.getOptionValue("b"),
        "-g", cl.getOptionValue("g"),
        "-name=mini",
        "-o", cl.getOptionValue("o")},
        bands.toArray(new String[bands.size()])));

    return mcj.getIndexPath().getParent().toString();
    }

  private static String alignReads(CommandLine cl, String indexPath) throws Exception
    {
    log.info("*********** ALIGN CHR JOB *************");
    String output = indexPath.substring(0, indexPath.indexOf("/index"));
    BWAAlign ba = new BWAAlign();
    ToolRunner.run(ba, new String[]{"-bwa-path", cl.getOptionValue("b"), "-n", "mini", "-i", indexPath, "-r", cl.getOptionValue("r"), "-o", output});
    ba.mergeSAM();
    return ba.getOutputPath().toString();
    }


  }
