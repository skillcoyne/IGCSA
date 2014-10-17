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
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.population.MinimalKaryotype;
import org.lcsb.lu.igcsa.population.PopulationGenerator;
import org.lcsb.lu.igcsa.job.*;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;

import java.util.*;


//** Need to update this to be a pipeline that starts from random selection
public class RandomSearchPipeline extends SearchPipeline
  {
  private static final Log log = LogFactory.getLog(RandomSearchPipeline.class);

  @Override
  public void setOptions()
    {
    options = new Options();
    options.addOption(new Option("b", "bwa-path", true, "bwa archive location"));
    options.addOption(new Option("o", "output", true, "output path"));
    options.addOption(new Option("g", "genome", true, "parent genome name for sequence generation"));
    options.addOption(new Option("r", "reads", true, "read path for tsv"));
    }

  @Override
  protected void usage()
    {
    HelpFormatter help = new HelpFormatter();
    if ( !commandLine.hasOption("b") | !commandLine.hasOption("o") | !commandLine.hasOption("g") | !commandLine.hasOption("r"))
      {
      help.printHelp(this.getClass().getSimpleName() + ":\nMissing required option. ", this.getOptions());
      System.exit(-1);
      }
    }

  public RandomSearchPipeline()
    {

    }

  //  # 1. Generate mini aberrations
  //  # 2. Index & Align
  //  # 3. Score
  //  # 4. Generate neighboring aberrations & ...?
  //  # 5. Repeat 2-4 until??

  public static void main(String[] args) throws Exception
    {
    SearchPipeline pipeline = new RandomSearchPipeline();
    CommandLine cl = pipeline.parseCommandLine(args);

    PopulationGenerator pg = new PopulationGenerator();
    List<MinimalKaryotype> pop = pg.run(1000, 75);

    pg.getObserver().finalUpdate();

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
      MiniChromosomeJob mcj = generateMiniAbrs(cl, abr);
      String alignPath = pipeline.alignReads(mcj.getIndexPath().getParent().toString(), mcj.getName());
      log.info(alignPath);
      break;
      }

    }

  private static MiniChromosomeJob generateMiniAbrs(CommandLine cl, Aberration abr) throws Exception
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
        "-n", "mini",
        "-o", cl.getOptionValue("o")},
        bands.toArray(new String[bands.size()])));

    return mcj;
    }

  }
